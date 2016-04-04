package ch.hsr.ifs.cute.templator.plugin.asttools;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

/**
 * Static methods for {@code IASTNode}s and {@code IASTName}s that have all information on their own and do not require
 * the {@code IIndex} for a global overview like {@link ASTAnalyzer}.
 */
public final class ASTTools {
	private ASTTools() {
	}

	/**
	 * The maximum offset for a file and length {@code 0} returns the root node (an {@code IASTTranslationUnit} and
	 * <b>NOT</b> {@code null}.
	 *
	 * @return the smallest {@code IASTName} enclosing in the given range (impossible to return {@code null} if there is
	 *         no other error)
	 * @throws IllegalArgumentException
	 *             if {@code region} or {@code ast} is null
	 */
	public static IASTName getNameAtRegion(IRegion region, IASTTranslationUnit ast) {
		IASTNode node = getNodeAtRegion(region, ast);
		if (node instanceof IASTName) {
			return (IASTName) node;
		}
		return null;
	}

	/**
	 * The maximum offset for a file and length {@code 0} returns the root node (an {@code IASTTranslationUnit} and
	 * <b>NOT</b> {@code null}.
	 *
	 * @return the smallest {@code IASTNode} enclosing the given range (impossible to return null if there is no other
	 *         error)
	 * @throws IllegalArgumentException
	 *             if {@code region} or {@code ast} is null
	 */
	public static IASTNode getNodeAtRegion(IRegion region, IASTTranslationUnit ast) {
		if (ast == null) {
			throw new IllegalArgumentException("getNodeAtRegion: IASTTranslationUnit must not be null");
		}
		if (region == null) {
			throw new IllegalArgumentException("getNodeAtRegion: IRegion must not be null");
		}

		IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		return nodeSelector.findEnclosingNode(region.getOffset(), region.getLength());
	}

	/**
	 * See {@link #findFirstAncestorByType(IASTNode, Class, int)}, last argument is {@code Integer.MAX_VALUE}.
	 */
	public static <T extends IASTNode> T findFirstAncestorByType(IASTNode node, Class<T> type)
			throws TemplatorException {
		return findFirstAncestorByType(node, type, Integer.MAX_VALUE);
	}

	/**
	 * Searches the AST from a given {@code IASTName name} to the top and returns the first found parent node for the
	 * given class {@code type}, otherwise {@code null} is returned.
	 *
	 * @param node
	 *            name where to start the search to the top
	 * @param type
	 *            class type to check for with {@code instanceof}
	 * @param maxLevel
	 *            upper limit of parents that are considered. Negative values result in immediate returns.
	 *
	 * @return the first parent with this type or {@code null}
	 * @throws TemplatorException
	 *             if the given {@code node} is null
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IASTNode> T findFirstAncestorByType(IASTNode node, Class<T> type, int maxLevel)
			throws TemplatorException {
		int remainingLevels = maxLevel;
		if (node == null) {
			throw new TemplatorException("Passed AST node to find its ancestors was null.");
		}

		IASTNode parent = node.getParent();

		while (parent != null && remainingLevels > 0) {
			if (type.isAssignableFrom(parent.getClass())) {
				return (T) parent;
			}
			parent = parent.getParent();
			remainingLevels--;
		}

		return null;
	}

	public static boolean isRelevantBinding(IBinding binding, boolean acceptUnknownBindings,
			boolean acceptNormalFunctions) {
		if (binding instanceof IFunction && acceptNormalFunctions) {
			return true;
		}
		return isTemplateDependentBinding(binding, acceptUnknownBindings);
	}

	public static boolean isTemplateDependentBinding(IBinding binding, boolean acceptUnknownBindings) {
		boolean isFunction = binding instanceof IFunction;
		boolean isSpecialization = binding instanceof ICPPSpecialization;

		if (isFunction) {
			return isSpecialization || (binding instanceof ICPPUnknownBinding && acceptUnknownBindings);
		} else {
			// because resolved class instances are instanceof ICPPSpecialization but may still be unknown. The template
			// parameter map is still known for deferred class instances because the name is known and all templates of
			// this name (primary, partially and fully specialized) have the same template parameter map

			//@formatter:off
            /*
             * accept is an unsymmetrical XNOR
             * acceptUnknownBindings, binding instanceof ICPPUnknownBinding = accept
             * true, true = true;
             * true, false = true;
             * false, true = false;
             * false, false = true;
             */
            //@formatter:on
			boolean acceptUnknown = (acceptUnknownBindings
					| !(acceptUnknownBindings ^ binding instanceof ICPPUnknownBinding));

			return (isSpecialization && acceptUnknown);
		}
	}

	public static boolean isMaybeAmbiguousBecauseOfTemplate(IBinding binding) {
		boolean maybeAmbiguousBecauseOfTemplate = false;
		if (binding instanceof IProblemBinding) {
			IProblemBinding problem = (IProblemBinding) binding;
			if (problem.getID() == IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP) {
				for (IBinding problemCandidate : problem.getCandidateBindings()) {
					if (problemCandidate instanceof ICPPSpecialization) {
						maybeAmbiguousBecauseOfTemplate = true;
						break;
					}
				}
			}
		}

		return maybeAmbiguousBecauseOfTemplate;
	}

	public static IASTName getName(ICPPASTFunctionCallExpression call) {
		IASTName name = null;
		IASTExpression functionNameExpression = call.getFunctionNameExpression();
		if (functionNameExpression instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression) functionNameExpression;
			name = idExpression.getName().getLastName();
			// contains <>, so get the template id
			if (name instanceof ICPPASTTemplateId) {
				ICPPASTTemplateId templateId = (ICPPASTTemplateId) name;
				name = templateId.getTemplateName();
			}
		} else if (functionNameExpression instanceof ICPPASTFieldReference) {
			name = ((ICPPASTFieldReference) functionNameExpression).getFieldName();
		}
		return name;
	}

	public static IASTName getName(IASTSimpleDeclaration simpleDeclaration) {
		if (simpleDeclaration != null) {
			IASTDeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
			return getName(declSpecifier);
		}
		return null;
	}

	public static IASTName getName(IASTDeclSpecifier declSpecifier) {
		if (declSpecifier instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier namedSpecifier = (IASTNamedTypeSpecifier) declSpecifier;
			return namedSpecifier.getName().getLastName();
		}
		return null;
	}

	public static IASTName getName(ICPPASTTypeId typeId) {
		if (typeId != null) {
			IASTDeclSpecifier declSpecifier = typeId.getDeclSpecifier();
			return getName(declSpecifier);
		}

		return null;
	}

	/**
	 * Returns the {@code IASTName} for a template instance that can be used to resolve to a function or class template.
	 * <ul>
	 * <li>{@code templatecall(1)} can just be resolved and returns an ICPPTemplateInstance</li>
	 *
	 * <li>{@code templatecall<int>(1)} can not just be resolved. Need to get the parent ({@code ICPPASTTemplateId}) and
	 * resolve this</li>
	 *
	 * <li>{@code namespace::templatecall<int>(1)} and without {@code <>} also works, but does not need anything
	 * specific since the qualifiername is the parent of the {@code IASTName} we are interested in</li>
	 * </ul>
	 *
	 * @param templateInstanceName
	 *            name to extract the template name from
	 * @return {@code IASTName} where {@code resolveBinding()} can be called to get template definition
	 * @throws TemplatorException
	 *             if {@code getParent()} on the given {@code name} is {@code null}
	 */
	public static IASTName extractTemplateInstanceName(IASTName templateInstanceName) throws TemplatorException {
		IASTName lastName = templateInstanceName.getLastName();
		if (lastName instanceof ICPPASTTemplateId) {
			return lastName;
		}

		IASTNode parentNode = lastName.getParent();
		if (parentNode == null) {
			throw new TemplatorException("parent of templateInstanceName cannot be null");
		}

		if (parentNode instanceof ICPPASTTemplateId) {
			return (IASTName) parentNode;
		}
		return templateInstanceName;
	}

	public static IASTNode getBody(IASTDeclaration definition) {
		IASTNode body = null;
		if (definition instanceof IASTFunctionDefinition) {
			body = ((IASTFunctionDefinition) definition).getBody();
		} else if (definition instanceof ICPPASTTemplateDeclaration) {
			body = getTemplateBody((ICPPASTTemplateDeclaration) definition);
		}

		return body;
	}

	public static IASTNode getTemplateBody(ICPPASTTemplateDeclaration templateDeclaration) {
		IASTNode[] declarationChildren = templateDeclaration.getDeclaration().getChildren();
		// find the correct child node to get the body of the template
		IASTNode templateBody = null;
		for (IASTNode declChild : declarationChildren) {
			if (declChild instanceof ICPPASTCompositeTypeSpecifier) {
				templateBody = declChild;
				break;
			}
		}
		return templateBody;
	}

	public static IASTName getDeclSpecifierName(IASTNode param) throws TemplatorException {
		IASTName declSpecifierName = null;
		ICPPASTParameterDeclaration parameterDeclaration = ASTTools.findFirstAncestorByType(param,
				ICPPASTParameterDeclaration.class, 5);
		if (parameterDeclaration != null) {
			IASTDeclSpecifier declSpecifier = parameterDeclaration.getDeclSpecifier();
			if (declSpecifier instanceof IASTNamedTypeSpecifier) {
				declSpecifierName = ((IASTNamedTypeSpecifier) declSpecifier).getName().getLastName();
			}
		}

		return declSpecifierName;
	}

	public static IBinding getUltimateBindingType(IASTName identifier) throws TemplatorException {
		if (identifier == null) {
			return null;
		}

		IBinding result = identifier.resolveBinding();
		if (result instanceof IType) {
			IType ultimateType = SemanticUtil.getUltimateType((IType) result, false);
			if (ultimateType instanceof IBinding) {
				result = (IBinding) ultimateType;
			}
		}

		return result;
	}

	/**
	 * @return the decl-specifier for the given parameter definition name. {@code null} for non named parameters like
	 *         char, int etc.
	 */
	public static IASTName getParameterTypeFromDefinitionName(IASTName parameterDefinitionName)
			throws TemplatorException {
		IASTName declSpecifier = ASTTools.getDeclSpecifierName(parameterDefinitionName);
		return declSpecifier;
	}

	public static IASTName getAliasedTypeFromDefinitionName(IASTName definitionName) throws TemplatorException {
		IASTName aliasedTypeName = getTypedefTypeFromDefinitionName(definitionName);
		if (aliasedTypeName == null) {
			aliasedTypeName = getUsingTypeFromDefinitionName(definitionName);
		}
		return aliasedTypeName;
	}

	public static IASTName getTypedefTypeFromDefinitionName(IASTName typedefDefinitionName) throws TemplatorException {
		IASTSimpleDeclaration declaration = ASTTools.findFirstAncestorByType(typedefDefinitionName,
				IASTSimpleDeclaration.class, 3);
		return ASTTools.getName(declaration);
	}

	public static IASTName getUsingTypeFromDefinitionName(IASTName usingDefinitionName) throws TemplatorException {
		ICPPASTAliasDeclaration aliasDeclaration = ASTTools.findFirstAncestorByType(usingDefinitionName,
				ICPPASTAliasDeclaration.class, 7);
		ICPPASTTypeId typeId = aliasDeclaration.getMappingTypeId();
		return getName(typeId);
	}
}
