package ch.hsr.ifs.cute.refactoringPreview.clonewar.app;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.ETTPFunctionTransform;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.ETTPTypeTransform;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.Transform;

/**
 * Entry point for the clonewar refactoring plugin. Based on the selected AST
 * Node the appropriate transformation is chosen and applied.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class CloneWarRefactoring extends CRefactoring {
    private Transform transformation_;

    /**
     * {@inheritDoc}
     */
    public CloneWarRefactoring(IFile file, ISelection selection,
            ICElement element, ICProject proj) {
        super(file, selection, element, proj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RefactoringDescriptor getRefactoringDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        RefactoringStatus status = super.checkInitialConditions(pm);
        determineRefactoringType(status);
        if (status.hasError())
            return status;
        transformation_.preprocess(status);
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        RefactoringStatus status = super.checkFinalConditions(pm);
        transformation_.postprocess(status);
        return status;
    }

    /**
     * Returns the transformation of this refactoring.
     * 
     * @return Transformation.
     */
    public Transform getTransformation() {
        return transformation_;
    }

    /**
     * Try to find an appropriate refactoring based on the user selection (AST
     * Node).
     * 
     * @param status
     *            A success refactoring status if an appropriate refactoring was
     *            found, otherwise error.
     */
    private void determineRefactoringType(RefactoringStatus status) {
        RefactoringResolver resolver = new RefactoringResolver(region);
        getUnit().accept(resolver);
        if (!resolver.foundRefactoring()) {
            status.addError("No type/function selected!");
            return;
        }
        transformation_ = resolver.getRefactoring();
        transformation_.setTranslationUnit(getUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void collectModifications(IProgressMonitor pm,
            ModificationCollector collector) throws CoreException,
            OperationCanceledException {
        transformation_.performChanges(collector);
    }

    /**
     * Helper class deciding which refactoring to apply, based on the selected
     * node.
     * 
     * @author ythrier(at)hsr.ch
     */
    private class RefactoringResolver extends CPPASTVisitor {
        private Region region_;
        private Transform refactoring_;

        /**
         * Create the refactoring resolver.
         * 
         * @param region
         *            Region to search.
         */
        public RefactoringResolver(Region region) {
            this.shouldVisitDeclarations = true;
            this.shouldVisitDeclSpecifiers = true;
            this.region_ = region;
        }

        /**
         * Returns true if an appropriate refactoring was found for the
         * currently selected node.
         * 
         * @return True if a refactoring can be performed for the selected node,
         *         otherwise false.
         */
        public boolean foundRefactoring() {
            return refactoring_ != null;
        }

        /**
         * Returns the refactoring the can be performed based on the selected
         * AST node found in the translation unit.
         * 
         * @return Refactoring that can be performed.
         */
        public Transform getRefactoring() {
            return refactoring_;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int leave(IASTDeclaration declaration) {
            if (isSelectedNode(declaration)) {
                if (isFunction(declaration)) {
                    refactoring_ = new ETTPFunctionTransform();
                    refactoring_.setNode(declaration);
                    return PROCESS_ABORT;
                }
                ICPPASTCompositeTypeSpecifier type = findTypeDef(declaration);
                if ((type != null) && isType(type)) {
                    refactoring_ = new ETTPTypeTransform();
                    refactoring_.setNode(type.getParent());
                    refactoring_
                            .setSingleSelection(((CPPASTSimpleDeclaration) declaration)
                                    .getDeclSpecifier());
                    return PROCESS_ABORT;
                }
            }
            return PROCESS_CONTINUE;
        }

        /**
         * Find the defintion of a type (struct/class).
         * 
         * @param declaration
         *            Declaration.
         * @return Type definition.
         */
        private CPPASTCompositeTypeSpecifier findTypeDef(
                IASTDeclaration declaration) {
            IASTNode node = declaration;
            while ((node != null)
                    && !(node instanceof ICPPASTCompositeTypeSpecifier))
                node = node.getParent();
            return (CPPASTCompositeTypeSpecifier) node;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int leave(IASTDeclSpecifier declSpec) {
            if (isSelectedNode(declSpec)) {
                int status = PROCESS_CONTINUE;
                IASTDeclaration decl = findFunctionDef(declSpec);
                if (decl != null && isFunction(decl)) {
                    status = leave(findFunctionDef(declSpec));
                    refactoring_.setSingleSelection(declSpec);
                }
                if (isType(declSpec)) {
                    refactoring_ = new ETTPTypeTransform();
                    refactoring_.setNode(declSpec.getParent());
                    status = PROCESS_ABORT;
                }
                return status;
            }
            return PROCESS_CONTINUE;
        }

        /**
         * Check if a given selection is a type (struct/class).
         * 
         * @param node
         *            Node.
         * @return True if the node is a type, otherwise false.
         */
        private boolean isType(IASTNode node) {
            return (node instanceof CPPASTCompositeTypeSpecifier);
        }

        /**
         * Walk up the selection path until the function node was found.
         * 
         * @param declSpec
         *            Declaration specifier.
         * @return Process abort flag if the the type is
         */
        private IASTDeclaration findFunctionDef(IASTDeclSpecifier declSpec) {
            IASTNode node = declSpec;
            while ((node != null)
                    && !(node instanceof ICPPASTFunctionDefinition))
                node = node.getParent();
            return (IASTDeclaration) node;
        }

        /**
         * Check if the node is a function.
         * 
         * @param node
         *            Node.
         * @return True if the node is a function, otherwise false.
         */
        private boolean isFunction(IASTNode node) {
            return (node instanceof ICPPASTFunctionDefinition);
        }

        /**
         * Check if the passed node is selected by the user.
         * 
         * @param node
         *            Node to check.
         * @return True if this node is selected, otherwise false.
         */
        private boolean isSelectedNode(IASTNode node) {
            return SelectionHelper.isSelectionOnExpression(region_, node);
        }
    }
}