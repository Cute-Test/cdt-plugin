package ch.hsr.ifs.mockator.tests.extractinterface;

import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.head;
import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.list;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.wrappers.SelectionHelper;
import ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.extractinterface.preconditions.ClassDefinitionLookup;
import ch.hsr.ifs.mockator.plugin.extractinterface.preconditions.MemFunCollector;
import ch.hsr.ifs.mockator.tests.AbstractRefactoringTest;


public class ExtractInterfaceRefactoringTest extends AbstractRefactoringTest {

   private static final String INTERFACE_NAME_DEFAULT = "FooInterface";
   private String              newInterfaceName;
   private boolean             replaceOccurrences;
   private String              funNames;
   private String              tuOfChosenClass;
   private String              warning;

   @Override
   protected Refactoring createRefactoring() {
      return new ExtractInterfaceRefactoring(buildRefactoringContext());
   }

   private ExtractInterfaceContext buildRefactoringContext() {
      return new ExtractInterfaceContext.ContextBuilder(getTu(getActiveCElement()), cproject, selection).replaceAllOccurences(replaceOccurrences)
               .withRefactoringStatus(new RefactoringStatus()).withNewInterfaceName(newInterfaceName).build();
   }

   @Override
   protected void configureTest(final Properties properties) {
      super.configureTest(properties);
      replaceOccurrences = Boolean.valueOf(properties.getProperty("replaceOccurrences", "true")).booleanValue();
      newInterfaceName = properties.getProperty("nameOfNewInterface", INTERFACE_NAME_DEFAULT);
      funNames = properties.getProperty("takeMemFuns", "");
      warning = properties.getProperty("warning", "");
      tuOfChosenClass = properties.getProperty("tuOfChosenClass", "");
   }

   @Override
   protected void assertStatusWarning(final RefactoringStatus status, final int number) {
      super.assertStatusWarning(status, number);
      if (!warning.equals("")) {
         final String warningMessage = status.getMessageMatchingSeverity(RefactoringStatus.WARNING);
         final String assertFailingMsg = "Warning '" + warning + "' in condition expected";
         assertTrue(assertFailingMsg, warningMessage != null && warningMessage.startsWith(warning));
      }
   }

   @Override
   protected void simulateUserInput(final RefactoringContext context) {
      final CRefactoringContext ccontext = (CRefactoringContext) context;
      if (ccontext != null) {
         final ExtractInterfaceContext eiContext = ((ExtractInterfaceRefactoring) context.getRefactoring()).getContext();
         eiContext.setCRefContext(ccontext);
         final IASTTranslationUnit ast = getAst(ccontext);
         eiContext.setSelectedName(getSelectedName(ast));
         new ClassDefinitionLookup().accept(eiContext);
         if (eiContext.getChosenClass() != null) {
            new MemFunCollector().accept(eiContext);
         }
         eiContext.setChosenMemFuns(getChosenMemFuns(eiContext));
         if (!tuOfChosenClass.equals("")) {
            eiContext.setTuOfChosenClass(getTuOfChosenClass(ccontext));
         } else {
            eiContext.setTuOfChosenClass(ast);
         }
      }
   }

   private IASTTranslationUnit getTuOfChosenClass(final CRefactoringContext context) {
      try {
         IFile chosenClassFile = getIFile(tuOfChosenClass);
         if (!referencedProjects.isEmpty()) {
            chosenClassFile = referencedProjects.get(0).getProject().getFile(tuOfChosenClass);
         }
         final ICElement chosenClass = CoreModel.getDefault().create(chosenClassFile);
         final ITranslationUnit tu = getTu(chosenClass);
         return context.getAST(tu, new NullProgressMonitor());
      } catch (final CoreException e) {}
      fail("Not able to get AST for translation unit");
      return null;
   }

   private Optional<IASTName> getSelectedName(final IASTTranslationUnit ast) {
      final Region region = SelectionHelper.getRegion(selection);
      return head(findAllMarkedNames(ast, region));
   }

   private static List<IASTName> findAllMarkedNames(final IASTTranslationUnit ast, final Region region) {
      final List<IASTName> names = list();
      ast.accept(new ASTVisitor() {

         {
            shouldVisitNames = true;
         }

         @Override
         public int visit(final IASTName name) {
            if (name.isPartOfTranslationUnitFile() && SelectionHelper.isNodeInsideSelection(name, region)
                && !(name instanceof ICPPASTQualifiedName)) {
               names.add(name);
            }
            return PROCESS_CONTINUE;
         }
      });
      return names;
   }

   private Collection<IASTDeclaration> getChosenMemFuns(final ExtractInterfaceContext context) {
      if (funNames.isEmpty()) {
         return context.getUsedPublicMemFuns();
      }

      final List<IASTDeclaration> chosenMemFuns = list();
      final List<String> chosenMemFunNames = list(funNames.split(","));

      for (final IASTDeclaration decl : context.getAvailablePupMemFuns()) {
         final IASTDeclarator declarator = ASTUtil.getDeclaratorForNode(decl);
         final String memFunName = declarator.getName().toString();

         if (chosenMemFunNames.contains(memFunName)) {
            chosenMemFuns.add(decl);
         }
      }
      return chosenMemFuns;
   }
}
