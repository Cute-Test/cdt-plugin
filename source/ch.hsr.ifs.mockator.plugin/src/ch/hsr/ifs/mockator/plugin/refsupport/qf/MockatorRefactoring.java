package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.array;
import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.last;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
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

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.ProjectUtil;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoring;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ClassInSelectionFinder;

public abstract class MockatorRefactoring extends CRefactoring {

   protected static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final ITextSelection           selection;

   public MockatorRefactoring(final ICElement element, final ITextSelection selection, final ICProject project) {
      super(element, selection, project);
      this.selection = selection;
      saveAllDirtyEditors();
   }

   private void saveAllDirtyEditors() {
      UiUtil.runInDisplayThread((ignored) -> {
         if (!IDE.saveAllEditors(getWorkspaceRoot(), false)) {
            initStatus().addFatalError("Was not able to save all editors");
         }
      }, initStatus());
   }

   private static IResource[] getWorkspaceRoot() {
      return new IResource[] { ProjectUtil.getWorkspaceRoot() };
   }

   protected ITextSelection getSelection() {
      return selection;
   }

   public abstract String getDescription();

   protected ASTRewrite createRewriter(final ModificationCollector collector, final IASTTranslationUnit ast) {
      return collector.rewriterForTranslationUnit(ast);
   }

   protected Optional<ICPPASTCompositeTypeSpecifier> getClassInSelection(final IASTTranslationUnit ast) {
      final ClassInSelectionFinder finder = new ClassInSelectionFinder(getSelection(), ast);
      return finder.getClassInSelection();
   }

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
      return ASTUtil.getAncestorOfType(name, ICPPASTFunctionDefinition.class);
   }

   @Override
   protected RefactoringStatus checkFinalConditions(final IProgressMonitor subProgressMonitor, final CheckConditionsContext checkContext)
            throws CoreException, OperationCanceledException {
      return initStatus();
   }

   @Override
   protected RefactoringDescriptor getRefactoringDescriptor() {
      return null;
   }

   protected Optional<IASTName> getSelectedName(final IASTTranslationUnit ast) {
      final IASTNode selectedNode = getSelectedNode(ast);

      if (selectedNode instanceof IASTImplicitNameOwner && ((IASTImplicitNameOwner) selectedNode).getImplicitNames().length > 0) {
         return findOperator(selectedNode);
      }

      if (selectedNode instanceof IASTName) {
         return Optional.of((IASTName) selectedNode);
      }

      final IASTName name = ASTUtil.getAncestorOfType(selectedNode, IASTName.class);

      if (name != null) {
         return Optional.of(name);
      }

      return last(findAllMarkedNames(ast));
   }

   private static Optional<IASTName> findOperator(final IASTNode selectedNode) {
      for (final IASTImplicitName iName : getNames(selectedNode)) {
         if (iName != null && iName.isOperator()) {
            return Optional.of((IASTName) iName);
         }
      }

      return Optional.empty();
   }

   private static IASTImplicitName[] getNames(final IASTNode selectedNode) {
      if (selectedNode instanceof ICPPASTUnaryExpression || selectedNode instanceof ICPPASTBinaryExpression
          || selectedNode instanceof ICPPASTNewExpression || selectedNode instanceof ICPPASTDeleteExpression) {
         return ((IASTImplicitNameOwner) selectedNode).getImplicitNames();
      }

      return array();
   }

   protected IASTNode getSelectedNode(final IASTTranslationUnit ast) {
      final String rootSourceOfTu = null;
      return ast.getNodeSelector(rootSourceOfTu).findEnclosingNodeInExpansion(selection.getOffset(), selection.getLength());
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
