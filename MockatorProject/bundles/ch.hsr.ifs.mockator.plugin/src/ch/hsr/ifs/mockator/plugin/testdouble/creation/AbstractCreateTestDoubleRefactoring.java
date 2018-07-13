package ch.hsr.ifs.mockator.plugin.testdouble.creation;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


public abstract class AbstractCreateTestDoubleRefactoring extends MockatorRefactoring {

   public AbstractCreateTestDoubleRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final ICProject cProject) {
      super(cElement, selection, cProject);
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
      final RefactoringStatus status = super.checkInitialConditions(pm);

      checkSelectedNameIsInFunction(status, pm).ifPresent((name) -> {
         final IBinding binding = name.resolveBinding();

         if (!(binding instanceof IProblemBinding)) {
            status.addFatalError("Selected name refers to an existing entity");
         }
      });
      return status;
   }

   protected void insertBeforeCurrentStmt(final IASTDeclarationStatement testDouble, final IASTTranslationUnit ast, final ASTRewrite rewriter) {
      findFirstSelectedStmt(ast).ifPresent((stmt) -> rewriter.insertBefore(stmt.getParent(), stmt, testDouble, null));
   }

   private Optional<IASTStatement> findFirstSelectedStmt(final IASTTranslationUnit ast) {
      final IASTStatement stmt = CPPVisitor.findAncestorWithType(getSelectedNode(ast), IASTStatement.class).orElse(null);
      return Optional.of(stmt);
   }

   protected ICPPASTCompositeTypeSpecifier createNewTestDoubleClass(final String name) {
      final IASTName className = nodeFactory.newName(name.toCharArray());
      final int structClassType = IASTCompositeTypeSpecifier.k_struct;
      final ICPPASTCompositeTypeSpecifier newClass = nodeFactory.newCompositeTypeSpecifier(structClassType, className);
      return newClass;
   }

   @Override
   public String getDescription() {
      return I18N.CreateTestDoubleRefactoringDesc;
   }
}
