package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


public class MoveTestDoubleToNsRefactoring extends MockatorRefactoring {

   private final CppStandard         cppStd;
   private ICPPASTFunctionDefinition testFunction;

   public MoveTestDoubleToNsRefactoring(final CppStandard cppStd, final ICElement cElement, final ITextSelection selection,
                                        final ICProject cProject) {
      super(cElement, selection, cProject);
      this.cppStd = cppStd;
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
      final RefactoringStatus status = super.checkInitialConditions(pm);

      if (!getClassInSelection(getAST(tu(), pm)).isPresent()) {
         status.addFatalError("Could not find a class in the current selection");
         return status;
      }

      checkSelectedNameIsInFunction(status, pm);
      return status;
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
      final Optional<ICPPASTCompositeTypeSpecifier> clazz = getClassInSelection(getAST(tu(), pm));
      if (clazz.isPresent()) {
         final IASTTranslationUnit ast = getAST(tu(), pm);
         final ASTRewrite rewriter = createRewriter(collector, ast);
         testFunction = getParentFunction(clazz.get().getName());
         moveToNamespace(clazz.get(), rewriter);
      }
   }

   private void moveToNamespace(final ICPPASTCompositeTypeSpecifier optClass, final ASTRewrite rewriter) {
      new TestDoubleToNsMover(rewriter, cppStd).moveToNamespace(optClass);
   }

   public ICPPASTFunctionDefinition getTestFunction() {
      return testFunction;
   }

   @Override
   public String getDescription() {
      return I18N.MoveTestDoubleToNsRefactoringDesc;
   }
}
