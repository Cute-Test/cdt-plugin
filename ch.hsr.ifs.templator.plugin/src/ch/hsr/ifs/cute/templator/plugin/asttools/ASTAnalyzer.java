package ch.hsr.ifs.cute.templator.plugin.asttools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;

import ch.hsr.ifs.cute.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.asttools.resolving.DefinitionFinder;
import ch.hsr.ifs.cute.templator.plugin.asttools.resolving.nametype.NameTypeDeducer;
import ch.hsr.ifs.cute.templator.plugin.asttools.resolving.nametype.TypeNameToType;
import ch.hsr.ifs.cute.templator.plugin.asttools.type.finding.RelevantNameType;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

/**
 * Class to find all related information to a template instance like subcalls and function/template definitions.
 */
public class ASTAnalyzer {
	private IIndex index;
	private IASTTranslationUnit ast;
	private DefinitionFinder definitionFinder;
	private NameTypeDeducer typeDeducer;

	public ASTAnalyzer(IIndex index, IASTTranslationUnit ast) {
		this.index = index;
		this.ast = ast;
		definitionFinder = new DefinitionFinder(index, ast);
		typeDeducer = new NameTypeDeducer(this);
	}

	public IASTFunctionDefinition getFunctionDefinition(IBinding function) throws TemplatorException {
		IFunction functionBinding = getFunctionBinding(function);
		IASTName definitionName = getDefinition(functionBinding);

		return ASTTools.findFirstAncestorByType(definitionName, IASTFunctionDefinition.class, 6);
	}

	public IASTName getDefinition(IBinding binding) throws TemplatorException {
		return definitionFinder.findDefinition(binding);
	}

	public ICPPASTTemplateDeclaration getTemplateDeclaration(IBinding templateBinding) throws TemplatorException {
		ICPPASTTemplateDeclaration templateDeclaration = null;
		IASTName templateDefinitionName = null;

		if (templateBinding instanceof ICPPClassSpecialization) {
			ICPPTemplateInstance classInstance = (ICPPTemplateInstance) templateBinding;
			if (classInstance.isExplicitSpecialization()) {
				templateDefinitionName = getDefinition(classInstance);
			}
		}
		if (templateDefinitionName == null) {
			IBinding innerMostBinding = NameTypeDeducer.getInnerMostBinding(templateBinding);
			templateDefinitionName = getDefinition(innerMostBinding);
		}

		templateDeclaration = ASTTools.findFirstAncestorByType(templateDefinitionName, ICPPASTTemplateDeclaration.class,
				6);
		return templateDeclaration;
	}

	/**
	 * This method is unused at the moment but stays for later when member function declarations outside the
	 * ICPPASTTemplateDeclaration for the class should be found.
	 */
	public Map<IASTSimpleDeclaration, IASTFunctionDefinition> searchFunctionDeclarationsToDefinitions(
			ICPPASTTemplateDeclaration templateDeclaration) throws TemplatorException {

		Map<IASTSimpleDeclaration, IASTFunctionDefinition> functionToDeclarationMap = new HashMap<>();

		IASTNode templateBody = ASTTools.getTemplateBody(templateDeclaration);
		if (templateBody != null) {
			for (IASTNode child : templateBody.getChildren()) {
				if (child instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) child;
					for (IASTDeclarator decl : declaration.getDeclarators()) {
						if (decl instanceof ICPPASTFunctionDeclarator) {
							IASTName memberFunctionName = decl.getName();
							IBinding functionBinding = resolveTargetBinding(memberFunctionName);
							IASTFunctionDefinition functionDefinition = getFunctionDefinition(functionBinding);
							functionToDeclarationMap.put(declaration, functionDefinition);
						}
					}
				}
			}
		}

		return functionToDeclarationMap;
	}

	public IFunction getFunctionBinding(IBinding binding) throws TemplatorException {
		IBinding normalBinding = NameTypeDeducer.getInnerMostBinding(binding);
		if (!(normalBinding instanceof IFunction)) {
			throw new TemplatorException("Binding " + normalBinding + " with " + normalBinding.getClass()
					+ " cannot be casted to an IFunction.");
		}

		return (IFunction) normalBinding;
	}

	public IBinding resolveTargetBinding(final IBinding normalBinding) throws TemplatorException {
		IBinding targetBinding = normalBinding;
		if (index != null) {
			IBinding adapted = IndexAction.<IBinding> perform(index, new IIndexAction() {
				@SuppressWarnings("unchecked")
				@Override
				public IIndexBinding doAction(IIndex index) throws Exception {
					return index.adaptBinding(normalBinding);
				}
			});
			if (adapted != null) {
				targetBinding = adapted;
			}
		}

		return targetBinding;
	}

	public IBinding resolveTargetBinding(IASTName identifier) throws TemplatorException {
		if (identifier == null) {
			return null;
		}

		IBinding normalResolvedBinding = identifier.resolveBinding();
		IBinding targetBinding = resolveTargetBinding(normalResolvedBinding);
		if (targetBinding == null) {
			throw new TemplatorException("Binding for name " + identifier + " could not be resolved.");
		}

		return targetBinding;
	}

	public IIndex getIndex() {
		return index;
	}

	public IASTTranslationUnit getAst() {
		return ast;
	}

	public IASTName getDefinitionName(IASTName originalName, boolean acceptUnknownBindings) throws TemplatorException {
		IASTName definitionName = originalName;

		IASTNode parent = originalName.getParent();
		if (parent == null || parent.getParent() == null) {
			throw new TemplatorException("Parent of name cannot be null.");
		}

		IBinding originalNameBinding = definitionName.resolveBinding();

		/*
		 * it was NOT already a template-id or function class template name without template-id or a function template
		 * name that has been resolved and dependent on a template argument.
		 *
		 * originalNameBinding may be null for empty names that will be created for the following line void foobar(int&
		 * (*bar)(int&));. The inner parameter int& contains ICPPASTDeclarator for & which contains a IASTName node
		 * which has no string representation and thus no binding.
		 */
		if (originalNameBinding != null
				&& !ASTTools.isRelevantBinding(originalNameBinding, acceptUnknownBindings, true)) {
			definitionName = getDefinition(originalNameBinding);
		}

		if (definitionName == null) {
			throw new TemplatorException("Could not find declaration/definition for " + originalName);
		}
		return definitionName;
	}

	/**
	 * Returns the {@code IASTName} that can be used to resolve the binding to a template instance or an
	 * {@code IFunction}.
	 */
	public RelevantNameType extractResolvingName(IASTName originalName, boolean acceptUnknownBindings)
			throws TemplatorException {
		IASTName definition = getDefinitionName(originalName, acceptUnknownBindings);
		RelevantNameType relevantNameType = RelevantNameType.create(definition);
		if (relevantNameType != null) {
			IASTName typeName = relevantNameType.getTypeName();
			if (ASTTools.isRelevantBinding(resolveTargetBinding(typeName), acceptUnknownBindings, true)) {
				return relevantNameType;
			}
		}
		return null;
	}

	public TypeNameToType getType(IASTName resolvingName, AbstractResolvedNameInfo parent) throws TemplatorException {
		return typeDeducer.getType(resolvingName, parent);
	}

	public NameTypeDeducer getTypeDeducer() {
		return typeDeducer;
	}
}