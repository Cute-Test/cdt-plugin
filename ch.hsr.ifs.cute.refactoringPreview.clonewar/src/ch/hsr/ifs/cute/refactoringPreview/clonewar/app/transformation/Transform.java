package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.TransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action.ConfigChangeAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action.ExistingTemplateChangeAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action.NewTemplateChangeAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action.SingleSelectionChangeAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.ASTTypeVisitor;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.CPPASTNodeFactory;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;

/**
 * Transformation base class to apply different types of transformation based on
 * the selection.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public abstract class Transform {
    private List<ConfigChangeAction> configChanges_ = new ArrayList<ConfigChangeAction>();
    private CPPASTNodeFactory nodeFactory_ = new CPPASTNodeFactory();
    private TransformConfiguration configuration_;
    private IASTTranslationUnit translationUnit_;
    private IASTNode originalNode_;
    private IASTNode copyNode_;
    private IASTNode singleSelection_;

    /**
     * Set the transform configuration.
     * 
     * @param config
     *            Configuration.
     */
    protected void setTransformConfiguration(TransformConfiguration config) {
        this.configuration_ = config;
    }

    /**
     * Return the configuration.
     * 
     * @return Configuration.
     */
    public TransformConfiguration getConfig() {
        return configuration_;
    }

    /**
     * Set the translation unit.
     * 
     * @param translationUnit
     *            Translation unit.
     */
    public void setTranslationUnit(IASTTranslationUnit translationUnit) {
        this.translationUnit_ = translationUnit;
    }

    /**
     * Set the single selection node.
     * 
     * @param singleSelection
     *            Single selection node.
     */
    public void setSingleSelection(IASTNode singleSelection) {
        this.singleSelection_ = singleSelection;
    }

    /**
     * Return the single selection.
     * 
     * @return Single selection node.
     */
    protected IASTNode getSingleSelection() {
        return singleSelection_;
    }

    /**
     * Check if a single selection was made.
     * 
     * @return Single selection.
     */
    protected boolean hasSingleSelection() {
        return singleSelection_ != null;
    }

    /**
     * Get the translation unit.
     * 
     * @return Translation unit.
     */
    protected IASTTranslationUnit getUnit() {
        return translationUnit_;
    }

    /**
     * Set the original node and creates a copy.
     * 
     * @param originalNode
     *            Original node.
     */
    public void setNode(IASTNode originalNode) {
        this.originalNode_ = originalNode;
        this.copyNode_ = originalNode_.copy();
    }

    /**
     * Returns the original node (frozen).
     * 
     * @return Original node.
     */
    protected IASTNode getOriginalNode() {
        return originalNode_;
    }

    /**
     * Returns the copy node (not frozen).
     * 
     * @return Copy node.
     */
    protected IASTNode getCopyNode() {
        return copyNode_;
    }

    /**
     * Create a rewriter for the actual translation unit.
     * 
     * @param collector
     *            Modification collector.
     * @param translationUnit
     *            Translation unit.
     * @return AST rewriter.
     */
    protected ASTRewrite createRewriter(ModificationCollector collector,
            IASTTranslationUnit translationUnit) {
        return collector.rewriterForTranslationUnit(translationUnit);
    }

    /**
     * Preprocessing. Informations/Errors/Warnings are added to the status.
     * 
     * @param status
     *            Status collector.
     */
    public void preprocess(RefactoringStatus status) {
        ASTTypeVisitor typeVisitor = findTypes(status);
        setTransformConfiguration(new TransformConfiguration(
                typeVisitor.getActionMap()));
        addConfigChangeActions(configChanges_);
        applyConfigChanges(status);
    }

    /**
     * Apply configuration changes.
     * 
     * @param status
     *            Status.
     */
    private void applyConfigChanges(RefactoringStatus status) {
        for (ConfigChangeAction configChange : configChanges_) {
            configChange.applyChange(getConfig(), status);
        }
    }

    /**
     * Find types in AST.
     * 
     * @param status
     *            Status.
     * @return Type visitor to get type map.
     */
    private ASTTypeVisitor findTypes(RefactoringStatus status) {
        ASTTypeVisitor typeVisitor = new ASTTypeVisitor();
        getOriginalNode().accept(typeVisitor);
        typeVisitor.enableSecondRun();
        getCopyNode().accept(typeVisitor);
        if (typeVisitor.hasException()) {
            status.addFatalError(typeVisitor.getException().getMessage());
        }
        return typeVisitor;
    }

    /**
     * Preprocessing. Informations/Errors/Warnings are added to the status.
     * 
     * @param status
     *            Status collector.
     */
    public abstract void postprocess(RefactoringStatus status);

    /**
     * Perform the changes of the transformation.
     * 
     * @param collector
     *            Modification collector.
     */
    public void performChanges(ModificationCollector collector) {
        applyTypeInfoToActions();
        for (TransformAction action : getConfig().getAllActions()) {
            if (action.shouldPerform())
                action.performTransform();
        }
        modificationPostprocessing(collector);
        performTemplateDeclarationProcessing(collector);
    }

    /**
     * Set the type informations on the actions to resolve the template name.
     */
    private void applyTypeInfoToActions() {
        for (TypeInformation type : getConfig().getAllTypes()) {
            for (TransformAction action : getConfig().getActionsOf(type)) {
                action.setTypeInformation(type);
            }
        }
    }

    /**
     * Perform replace operation of old with new definition.
     * 
     * @param collector
     *            Modification collector.
     * @param oldNode
     *            Old node.
     * @param newNode
     *            New node.
     */
    private void performReplace(ModificationCollector collector,
            IASTNode oldNode, IASTNode newNode) {
        ASTRewrite rewriter = createRewriter(collector, translationUnit_);
        rewriter.replace(oldNode, newNode, createEditText());
    }

    /**
     * Perform the change or add operation for the template declaration.
     * 
     * @param collector
     */
    private void performTemplateDeclarationProcessing(
            ModificationCollector collector) {
        if (isTemplate())
            performTemplateChange(collector);
        else
            performTemplateAdd(collector);
    }

    /**
     * Add a template definition.
     * 
     * @param collector
     *            Modification collector.
     */
    private void performTemplateAdd(ModificationCollector collector) {
        performReplace(collector, getOriginalNode(),
                createTemplateDeclaration());
    }

    /**
     * Change a template definition.
     * 
     * @param collector
     *            Modification collector.
     */
    private void performTemplateChange(ModificationCollector collector) {
        performReplace(collector, getOriginalNode().getParent(),
                createTemplateDeclaration());
    }

    /**
     * Create the template declaration.
     * 
     * @return New template node.
     */
    private IASTNode createTemplateDeclaration() {
        IASTDeclaration templateBody = getTemplateBody();
        return nodeFactory_.createTemplateDeclaration(createTemplateParams(),
                templateBody);
    }

    /**
     * Return the template body.
     * 
     * @return Template body.
     */
    protected abstract IASTDeclaration getTemplateBody();

    /**
     * Create a template parameter.
     * 
     * @param templParams
     *            Template parameter list to add the parameter.
     * @param type
     *            Type information.
     */
    protected abstract void createTemplateParameter(
            List<ICPPASTSimpleTypeTemplateParameter> templParams,
            TypeInformation type);

    /**
     * Add config change actions for preprocessing adjustments of the
     * configuration.
     * 
     * @param configChanges
     *            List to add the config changes.
     */
    protected abstract void addConfigChangeActions(
            List<ConfigChangeAction> configChanges);

    /**
     * Make post-processing of the change.
     * 
     * @param collector
     *            Modification collector.
     */
    protected abstract void modificationPostprocessing(
            ModificationCollector collector);

    /**
     * Create the edit group text of the change.
     * 
     * @return Edit group.
     */
    protected abstract TextEditGroup createEditText();

    /**
     * Check if the function to transform is a template function.
     * 
     * @return True if the function to change is a template function, otherwise
     *         false.
     */
    protected boolean isTemplate() {
        return (getOriginalNode().getParent() instanceof ICPPASTTemplateDeclaration);
    }

    /**
     * Returns the template declaration of the selected node.
     * 
     * @return Template declaration.
     */
    protected ICPPASTTemplateDeclaration getTemplateDeclaration() {
        return getTemplateDeclaration(getOriginalNode());
    }

    /**
     * Get the node factory.
     * 
     * @return Node factory.
     */
    protected CPPASTNodeFactory getNodeFactory() {
        return nodeFactory_;
    }

    /**
     * Return the template declaration of the passed node.
     * 
     * @param node
     *            Node to get the template declaration from.
     * @return Template declaration.
     */
    private ICPPASTTemplateDeclaration getTemplateDeclaration(IASTNode node) {
        return (ICPPASTTemplateDeclaration) node.getParent();
    }

    /**
     * Add a name change action based on whether the function is a template or
     * not.
     * 
     * @param configChanges
     *            List to add the action to.
     */
    protected void addNameChangeAction(List<ConfigChangeAction> configChanges) {
        if (isTemplate())
            configChanges.add(new ExistingTemplateChangeAction(
                    getTemplateDeclaration()));
        else
            configChanges.add(new NewTemplateChangeAction());
    }

    /**
     * Add a single selection change action if a single selection was made.
     * 
     * @param configChanges
     *            List to add the action to.
     */
    protected void addSingleSelectionChangeAction(
            List<ConfigChangeAction> configChanges) {
        if (hasSingleSelection())
            configChanges.add(new SingleSelectionChangeAction(
                    getOriginalNode(), getCopyNode(), getSingleSelection()));
    }

    /**
     * Create an empty default declarator.
     * 
     * @return Declarator.
     */
    protected IASTDeclarator createDefaultDeclarator() {
        IASTDeclarator absDecl = new CPPASTDeclarator(new CPPASTName(
                new char[] {}));
        return absDecl;
    }

    /**
     * Create the template parameters.
     * 
     * @return List of template parameters.
     */
    protected List<ICPPASTSimpleTypeTemplateParameter> createTemplateParams() {
        List<ICPPASTSimpleTypeTemplateParameter> templParams = new ArrayList<ICPPASTSimpleTypeTemplateParameter>();
        Set<String> existingParams = new HashSet<String>();
        for (TypeInformation type : getConfig().getAllTypesOrdered()) {
            if (!existingParams.contains(type.getTemplateName())) {
                createTemplateParameter(templParams, type);
                existingParams.add(type.getTemplateName());
            }
        }
        return templParams;
    }

    /**
     * Create a template parameter.
     * 
     * @param type
     *            Type.
     * @return Template parameter.
     */
    protected ICPPASTSimpleTypeTemplateParameter createTemplateParam(
            TypeInformation type) {
        return getNodeFactory().createTemplateParameterDefinition(
                type.getTemplateName());
    }

    /**
     * Create a type id with default declarator.
     * 
     * @param type
     *            Type.
     * @return Type id.
     */
    protected CPPASTTypeId createTypeId(TypeInformation type) {
        if (type.hasDefaultType())
            return new CPPASTTypeId(type.getDefaultType().copy(),
                    createDefaultDeclarator());
        else
            return new CPPASTTypeId(type.getCallSpecificDefaultType().copy(),
                    createDefaultDeclarator());
    }
}