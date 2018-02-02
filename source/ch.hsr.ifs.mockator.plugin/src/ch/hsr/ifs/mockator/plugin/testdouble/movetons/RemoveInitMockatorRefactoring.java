package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.head;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
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

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CCompositeChange;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.properties.FunctionsToAnalyze;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.MacroFinderVisitor;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


public class RemoveInitMockatorRefactoring extends MockatorRefactoring {

   private final IDocument           doc;
   private ICPPASTFunctionDefinition testFunction;

   public RemoveInitMockatorRefactoring(final IDocument doc, final ICElement cElement, final ITextSelection selection, final ICProject cProject) {
      super(cElement, selection, cProject);
      this.doc = doc;
   }

   public void setTestFunction(final ICPPASTFunctionDefinition function) {
      testFunction = getUpdatedTestFunction(function);
   }

   // This hack is necessary to get the updated version of the test function
   private ICPPASTFunctionDefinition getUpdatedTestFunction(final ICPPASTFunctionDefinition testFunction) {
      try {
         final FunctionEquivalenceVerifier verifier = new FunctionEquivalenceVerifier((ICPPASTFunctionDeclarator) testFunction.getDeclarator());
         final Collection<IASTFunctionDefinition> testFunctions = getTestfunctionsInTu().stream().filter((funDef) -> verifier.isEquivalent(
                  (ICPPASTFunctionDeclarator) funDef.getDeclarator())).collect(Collectors.toList());
         ILTISException.Unless.isTrue(testFunctions.size() == 1, "Was not able not unambiguously determine test function");
         return (ICPPASTFunctionDefinition) head(testFunctions).get();
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private Collection<IASTFunctionDefinition> getTestfunctionsInTu() throws CoreException {
      final IASTTranslationUnit ast = getAST(tu(), new NullProgressMonitor());
      final TestFunctionFinderVisitor finder = new TestFunctionFinderVisitor(getFunctionsToAnalyze());
      ast.accept(finder);
      return finder.getFunctions();
   }

   private FunctionsToAnalyze getFunctionsToAnalyze() {
      return FunctionsToAnalyze.fromProjectSettings(getIProject());
   }

   @Override
   public Change createChange(final IProgressMonitor pm) throws CoreException {
      if (!hasMockatorInitCall()) {
         return new NullChange();
      }

      return createDeleteChange(pm);
   }

   private boolean hasMockatorInitCall() {
      final MacroFinderVisitor macroFinder = new MacroFinderVisitor(MockatorConstants.INIT_MOCKATOR);
      testFunction.accept(macroFinder);
      return !macroFinder.getMatchingMacroExpansions().isEmpty();
   }

   private Change createDeleteChange(final IProgressMonitor pm) throws CoreException {
      final IASTFileLocation funFileLocation = getFileLocationForTestFunction(pm);
      final int funNodeOffset = funFileLocation.getNodeOffset();
      final String funText = getFunctionText(doc, funNodeOffset, funFileLocation.getNodeLength());
      final String initMockatorMacro = getInitMockatorMacroText();
      final DeleteEdit deleteEdit = createDeleteEdit(funNodeOffset, funText, initMockatorMacro);
      return createChange(pm, deleteEdit);
   }

   private Change createChange(final IProgressMonitor pm, final DeleteEdit deleteEdit) throws CoreException {
      final MultiTextEdit multiTextEdit = new MultiTextEdit();
      multiTextEdit.addChild(deleteEdit);
      final TextFileChange change = createTextFileChange(multiTextEdit, pm);
      final CCompositeChange result = new CCompositeChange("");
      result.add(change);
      return result;
   }

   private static DeleteEdit createDeleteEdit(final int funNodeOffset, final String funText, final String initMockatorMacro) {
      final int initCallOffset = funText.indexOf(initMockatorMacro);
      return new DeleteEdit(funNodeOffset + initCallOffset, initMockatorMacro.length());
   }

   private IASTFileLocation getFileLocationForTestFunction(final IProgressMonitor pm) throws CoreException {
      final IASTFunctionDefinition function = getFunctionInCurrentAst(pm);
      return function.getFileLocation();
   }

   private static String getInitMockatorMacroText() {
      return MockatorConstants.INIT_MOCKATOR + MockatorConstants.L_PARENTHESIS + MockatorConstants.R_PARENTHESIS + MockatorConstants.SEMICOLON;
   }

   private static String getFunctionText(final IDocument doc, final int funNodeOffset, final int funNodeLength) {
      try {
         return doc.get(funNodeOffset, funNodeLength);
      } catch (final BadLocationException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private IASTFunctionDefinition getFunctionInCurrentAst(final IProgressMonitor pm) throws CoreException {
      return getMatchingTestFunction(getAllTestFunctions(pm));
   }

   private IASTFunctionDefinition getMatchingTestFunction(final Collection<IASTFunctionDefinition> allTestFunctions) {
      final String testFunName = testFunction.getDeclarator().getName().toString();
      final Collection<IASTFunctionDefinition> functions = allTestFunctions.stream().filter((funDef) -> funDef.getDeclarator().getName().toString()
               .equals(testFunName)).collect(Collectors.toList());
      ILTISException.Unless.isFalse(functions.isEmpty(), "Could not find test function");
      return head(functions).get();
   }

   private Collection<IASTFunctionDefinition> getAllTestFunctions(final IProgressMonitor pm) throws CoreException {
      final TestFunctionFinderVisitor finder = new TestFunctionFinderVisitor(getFunctionsToAnalyze());
      final IASTTranslationUnit ast = getAST(tu(), pm);
      ast.accept(finder);
      return finder.getFunctions();
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector c) throws CoreException, OperationCanceledException {}

   private TextFileChange createTextFileChange(final MultiTextEdit multiTextEdit, final IProgressMonitor pm) throws CoreException {
      final IASTTranslationUnit ast = getAST(tu(), pm);
      final TextFileChange change = new TextFileChange("Delete mockator init call", FileUtil.toIFile(ast.getFilePath()));
      change.setEdit(multiTextEdit);
      return change;
   }

   @Override
   public String getDescription() {
      return I18N.RemoveInitMockatorRefactoringDesc;
   }
}
