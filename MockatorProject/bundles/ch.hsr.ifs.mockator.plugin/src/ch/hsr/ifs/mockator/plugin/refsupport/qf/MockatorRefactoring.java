package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.array;
import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.last;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.core.resources.WorkspaceUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoring;

import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;


public abstract class MockatorRefactoring extends CRefactoring {

   protected static final String NO_CLASS_FOUND_IN_SELECTION = "Could not find a class in the current selection";

   protected static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   public MockatorRefactoring(final ICElement element, final Optional<ITextSelection> selection, final ICProject project) {
      super(element, selection, project);
      saveAllDirtyEditors();
   }

   private void saveAllDirtyEditors() {
      UiUtil.runInDisplayThread((ignored) -> {
         if (!IDE.saveAllEditors(getWorkspaceRoot(), false)) {
            initStatus.addFatalError("Was not able to save all editors");
         }
      }, initStatus);
   }

   private static IResource[] getWorkspaceRoot() {
      return new IResource[] { WorkspaceUtil.getWorkspaceRoot() };
   }

   public abstract String getDescription();

   protected Optional<IASTName> checkSelectedNameIsInFunction(final RefactoringStatus status, final IProgressMonitor pm) throws CoreException {
      final Optional<IASTName> selectedName = getSelectedName(getAST(getTranslationUnit(), pm));

      if (!selectedName.isPresent()) {
         status.addFatalError("Selection does not contain a name");
      }

      if (getParentFunction(selectedName.get()) == null) {
         status.addFatalError("Selection is not part of a function");
      }

      return selectedName;
   }

   protected ICPPASTFunctionDefinition getParentFunction(final IASTName name) {
      return CPPVisitor.findAncestorWithType(name, ICPPASTFunctionDefinition.class).orElse(null);
   }

   @Override
   protected RefactoringStatus checkFinalConditions(final IProgressMonitor subProgressMonitor, final CheckConditionsContext checkContext)
         throws CoreException, OperationCanceledException {
      return initStatus;
   }

   @Override
   protected RefactoringDescriptor getRefactoringDescriptor() {
      return null;
   }

   protected Optional<IASTName> getSelectedName(final IASTTranslationUnit ast) {
      final IASTNode selectedNode = getSelectedNode(ast);

      if (selectedNode instanceof IASTImplicitNameOwner && ((IASTImplicitNameOwner) selectedNode)
            .getImplicitNames().length > 0) { return findOperator(selectedNode); }

      if (selectedNode instanceof IASTName) { return Optional.of((IASTName) selectedNode); }

      final IASTName name = CPPVisitor.findAncestorWithType(selectedNode, IASTName.class).orElse(null);

      if (name != null) { return Optional.of(name); }

      return last(findAllMarkedNames(ast));
   }

   private static Optional<IASTName> findOperator(final IASTNode selectedNode) {
      for (final IASTImplicitName iName : getNames(selectedNode)) {
         if (iName != null && iName.isOperator()) { return Optional.of((IASTName) iName); }
      }

      return Optional.empty();
   }

   private static IASTImplicitName[] getNames(final IASTNode selectedNode) {
      if (selectedNode instanceof ICPPASTUnaryExpression || selectedNode instanceof ICPPASTBinaryExpression ||
          selectedNode instanceof ICPPASTNewExpression ||
          selectedNode instanceof ICPPASTDeleteExpression) { return ((IASTImplicitNameOwner) selectedNode).getImplicitNames(); }

      return array();
   }

   protected IASTNode getSelectedNode(final IASTTranslationUnit ast) {
      return ast.getNodeSelector(null).findEnclosingNodeInExpansion(selection.map(ITextSelection::getOffset).orElse(-1), selection.map(
            ITextSelection::getLength).orElse(-1));
   }

   @Override
   protected IIndex getIndex() {
      try {
         return super.getIndex();
      } catch (final OperationCanceledException e) {
         throw new ILTISException(e).rethrowUnchecked();
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
