package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.properties.FunctionsToAnalyze;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.MacroFinderVisitor;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;

@SuppressWarnings("restriction")
public class RemoveInitMockatorRefactoring extends MockatorRefactoring {
  private final IDocument doc;
  private ICPPASTFunctionDefinition testFunction;

  public RemoveInitMockatorRefactoring(IDocument doc, ICElement cElement, ITextSelection selection,
      ICProject cProject) {
    super(cElement, selection, cProject);
    this.doc = doc;
  }

  public void setTestFunction(ICPPASTFunctionDefinition function) {
    testFunction = getUpdatedTestFunction(function);
  }

  // This hack is necessary to get the updated version of the test function
  private ICPPASTFunctionDefinition getUpdatedTestFunction(ICPPASTFunctionDefinition testFunction) {
    try {
      final FunctionEquivalenceVerifier verifier =
          new FunctionEquivalenceVerifier((ICPPASTFunctionDeclarator) testFunction.getDeclarator());
      Collection<IASTFunctionDefinition> testFunctions =
          filter(getTestfunctionsInTu(), new F1<IASTFunctionDefinition, Boolean>() {
            @Override
            public Boolean apply(IASTFunctionDefinition funDef) {
              return verifier.isEquivalent((ICPPASTFunctionDeclarator) funDef.getDeclarator());
            }
          });
      Assert.isTrue(testFunctions.size() == 1,
          "Was not able not unambiguously determine test function");
      return (ICPPASTFunctionDefinition) head(testFunctions).get();
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private Collection<IASTFunctionDefinition> getTestfunctionsInTu() throws CoreException {
    IASTTranslationUnit ast = getAST(tu, new NullProgressMonitor());
    TestFunctionFinderVisitor finder = new TestFunctionFinderVisitor(getFunctionsToAnalyze());
    ast.accept(finder);
    return finder.getFunctions();
  }

  private FunctionsToAnalyze getFunctionsToAnalyze() {
    return FunctionsToAnalyze.fromProjectSettings(project.getProject());
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException {
    if (!hasMockatorInitCall())
      return new NullChange();

    return createDeleteChange(pm);
  }

  private boolean hasMockatorInitCall() {
    MacroFinderVisitor macroFinder = new MacroFinderVisitor(MockatorConstants.INIT_MOCKATOR);
    testFunction.accept(macroFinder);
    return !macroFinder.getMatchingMacroExpansions().isEmpty();
  }

  private Change createDeleteChange(IProgressMonitor pm) throws CoreException {
    IASTFileLocation funFileLocation = getFileLocationForTestFunction(pm);
    int funNodeOffset = funFileLocation.getNodeOffset();
    String funText = getFunctionText(doc, funNodeOffset, funFileLocation.getNodeLength());
    String initMockatorMacro = getInitMockatorMacroText();
    DeleteEdit deleteEdit = createDeleteEdit(funNodeOffset, funText, initMockatorMacro);
    return createChange(pm, deleteEdit);
  }

  private Change createChange(IProgressMonitor pm, DeleteEdit deleteEdit) throws CoreException {
    MultiTextEdit multiTextEdit = new MultiTextEdit();
    multiTextEdit.addChild(deleteEdit);
    TextFileChange change = createTextFileChange(multiTextEdit, pm);
    CCompositeChange result = new CCompositeChange("");
    result.add(change);
    return result;
  }

  private static DeleteEdit createDeleteEdit(int funNodeOffset, String funText,
      String initMockatorMacro) {
    int initCallOffset = funText.indexOf(initMockatorMacro);
    return new DeleteEdit(funNodeOffset + initCallOffset, initMockatorMacro.length());
  }

  private IASTFileLocation getFileLocationForTestFunction(IProgressMonitor pm) throws CoreException {
    IASTFunctionDefinition function = getFunctionInCurrentAst(pm);
    return function.getFileLocation();
  }

  private static String getInitMockatorMacroText() {
    return MockatorConstants.INIT_MOCKATOR + MockatorConstants.L_PARENTHESIS
        + MockatorConstants.R_PARENTHESIS + MockatorConstants.SEMICOLON;
  }

  private static String getFunctionText(IDocument doc, int funNodeOffset, int funNodeLength) {
    try {
      return doc.get(funNodeOffset, funNodeLength);
    } catch (BadLocationException e) {
      throw new MockatorException(e);
    }
  }

  private IASTFunctionDefinition getFunctionInCurrentAst(IProgressMonitor pm) throws CoreException {
    return getMatchingTestFunction(getAllTestFunctions(pm));
  }

  private IASTFunctionDefinition getMatchingTestFunction(
      Collection<IASTFunctionDefinition> allTestFunctions) {
    final String testFunName = testFunction.getDeclarator().getName().toString();
    Collection<IASTFunctionDefinition> functions =
        filter(allTestFunctions, new F1<IASTFunctionDefinition, Boolean>() {
          @Override
          public Boolean apply(IASTFunctionDefinition funDef) {
            return funDef.getDeclarator().getName().toString().equals(testFunName);
          }
        });
    Assert.isFalse(functions.isEmpty(), "Could not find test function");
    return head(functions).get();
  }

  private Collection<IASTFunctionDefinition> getAllTestFunctions(IProgressMonitor pm)
      throws CoreException {
    TestFunctionFinderVisitor finder = new TestFunctionFinderVisitor(getFunctionsToAnalyze());
    IASTTranslationUnit ast = getAST(tu, pm);
    ast.accept(finder);
    return finder.getFunctions();
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector c)
      throws CoreException, OperationCanceledException {}

  private TextFileChange createTextFileChange(MultiTextEdit multiTextEdit, IProgressMonitor pm)
      throws CoreException {
    IASTTranslationUnit ast = getAST(tu, pm);
    TextFileChange change =
        new TextFileChange("Delete mockator init call", FileUtil.toIFile(ast.getFilePath()));
    change.setEdit(multiTextEdit);
    return change;
  }

  @Override
  public String getDescription() {
    return I18N.RemoveInitMockatorRefactoringDesc;
  }
}
