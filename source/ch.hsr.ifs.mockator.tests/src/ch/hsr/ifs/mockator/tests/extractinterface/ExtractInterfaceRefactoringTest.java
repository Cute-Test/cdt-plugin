package ch.hsr.ifs.mockator.tests.extractinterface;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.extractinterface.preconditions.ClassDefinitionLookup;
import ch.hsr.ifs.mockator.plugin.extractinterface.preconditions.MemFunCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;

@SuppressWarnings("restriction")
public class ExtractInterfaceRefactoringTest extends MockatorRefactoringTest {
  private static final String INTERFACE_NAME_DEFAULT = "FooInterface";
  private String newInterfaceName;
  private boolean replaceOccurrences;
  private String funNames;
  private String tuOfChosenClass;
  private String warning;

  @Override
  protected Refactoring createRefactoring() {
    return new ExtractInterfaceRefactoring(buildRefactoringContext());
  }

  private ExtractInterfaceContext buildRefactoringContext() {
    return new ExtractInterfaceContext.ContextBuilder(getTu(getActiveCElement()), cproject,
        selection).replaceAllOccurences(replaceOccurrences)
        .withRefactoringStatus(new RefactoringStatus()).withNewInterfaceName(newInterfaceName)
        .build();
  }

  @Override
  protected void configureTest(Properties properties) {
    super.configureTest(properties);
    replaceOccurrences =
        Boolean.valueOf(properties.getProperty("replaceOccurrences", "true")).booleanValue();
    newInterfaceName = properties.getProperty("nameOfNewInterface", INTERFACE_NAME_DEFAULT);
    funNames = properties.getProperty("takeMemFuns", "");
    warning = properties.getProperty("warning", "");
    tuOfChosenClass = properties.getProperty("tuOfChosenClass", "");
  }

  @Override
  protected void assertStatusWarning(RefactoringStatus status, int number) {
    super.assertStatusWarning(status, number);
    if (!warning.equals("")) {
      String warningMessage = status.getMessageMatchingSeverity(RefactoringStatus.WARNING);
      String assertFailingMsg = "Warning '" + warning + "' in condition expected";
      assertTrue(assertFailingMsg, warningMessage != null && warningMessage.startsWith(warning));
    }
  }

  @Override
  protected void simulateUserInput(RefactoringContext context) {
    CRefactoringContext ccontext = (CRefactoringContext) context;
    ExtractInterfaceContext eiContext =
        ((ExtractInterfaceRefactoring) context.getRefactoring()).getContext();
    eiContext.setCRefContext(ccontext);
    IASTTranslationUnit ast = getAst(ccontext);
    eiContext.setSelectedName(getSelectedName(ast));
    new ClassDefinitionLookup().apply(eiContext);
    if (eiContext.getChosenClass() != null) {
      new MemFunCollector().apply(eiContext);
    }
    eiContext.setChosenMemFuns(getChosenMemFuns(eiContext));
    if (!tuOfChosenClass.equals("")) {
      eiContext.setTuOfChosenClass(getTuOfChosenClass(ccontext));
    } else {
      eiContext.setTuOfChosenClass(ast);
    }
  }

  private IASTTranslationUnit getTuOfChosenClass(CRefactoringContext context) {
    try {
      IFile chosenClassFile = getIFile(tuOfChosenClass);
      if (!referencedProjects.isEmpty()) {
        chosenClassFile = referencedProjects.get(0).getProject().getFile(tuOfChosenClass);
      }
      ICElement chosenClass = CoreModel.getDefault().create(chosenClassFile);
      ITranslationUnit tu = getTu(chosenClass);
      return context.getAST(tu, new NullProgressMonitor());
    } catch (CoreException e) {
    }
    fail("Not able to get AST for translation unit");
    return null;
  }

  private Maybe<IASTName> getSelectedName(IASTTranslationUnit ast) {
    Region region = SelectionHelper.getRegion(selection);
    return head(findAllMarkedNames(ast, region));
  }

  private static List<IASTName> findAllMarkedNames(IASTTranslationUnit ast, final Region region) {
    final List<IASTName> names = list();
    ast.accept(new ASTVisitor() {
      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
        if (name.isPartOfTranslationUnitFile()
            && SelectionHelper.isNodeInsideSelection(name, region)
            && !(name instanceof ICPPASTQualifiedName)) {
          names.add(name);
        }
        return PROCESS_CONTINUE;
      }
    });
    return names;
  }

  private Collection<IASTDeclaration> getChosenMemFuns(ExtractInterfaceContext context) {
    if (funNames.isEmpty())
      return context.getUsedPublicMemFuns();

    List<IASTDeclaration> chosenMemFuns = list();
    List<String> chosenMemFunNames = list(funNames.split(","));

    for (IASTDeclaration decl : context.getAvailablePupMemFuns()) {
      IASTDeclarator declarator = AstUtil.getDeclaratorForNode(decl);
      String memFunName = declarator.getName().toString();

      if (chosenMemFunNames.contains(memFunName)) {
        chosenMemFuns.add(decl);
      }
    }
    return chosenMemFuns;
  }
}
