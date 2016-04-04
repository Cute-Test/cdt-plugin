package ch.hsr.ifs.cute.templator.plugin.asttools.resolving;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import ch.hsr.ifs.cute.templator.plugin.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.templator.plugin.asttools.ASTTools;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.AbstractTemplateInstance;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.NameTypeKind;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.TemplateInstance;
import ch.hsr.ifs.cute.templator.plugin.asttools.data.UnresolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

/**
 * Resolving and instantiating deferred bindings based on the template argument map from the surrounding
 * {@code AbstractResolvedNameInfo}s. This happens after the normal binding resolution process.
 */
public final class PostResolver {
	private PostResolver() {
	}

	public static UnresolvedNameInfo resolveToFinalBinding(UnresolvedNameInfo unresolvedName,
			AbstractResolvedNameInfo parent, ASTAnalyzer analyzer) throws TemplatorException {

		IBinding resolvedTemplateInstance = resolveDeferredBinding(unresolvedName, parent, analyzer);
		unresolvedName.setBinding(resolvedTemplateInstance, true);

		return unresolvedName;
	}

	public static IBinding resolveDeferredBinding(UnresolvedNameInfo unresolvedName, AbstractResolvedNameInfo parent,
			ASTAnalyzer analyzer) throws TemplatorException {
		NameTypeKind type = unresolvedName.getType();
		IASTName resolvingName = unresolvedName.getResolvingName();

		if (type == null || !type.isDeferred()) {
			return unresolvedName.getBinding();
		}
		IBinding finalResolvedBinding = null;
		try {
			if (type == NameTypeKind.DEFERRED_FUNCTION && parent instanceof AbstractResolvedNameInfo) {
				finalResolvedBinding = resolveFunction(resolvingName, parent, analyzer);
			} else if (type == NameTypeKind.DEFERRED_CLASS_TEMPLATE) {
				ICPPClassSpecialization classInstance = resolveClassTemplate(resolvingName, parent, analyzer);
				if (classInstance != null) {
					finalResolvedBinding = classInstance;
				}
			} else if (type.isMember()) {
				finalResolvedBinding = ClassTemplateMemberResolver.resolveClassTemplateMember(unresolvedName, parent);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new TemplatorException(e);
		}
		return finalResolvedBinding;
	}

	public static IFunction resolveFunction(IASTName resolvingName, AbstractResolvedNameInfo parentInstance,
			ASTAnalyzer analyzer) throws TemplatorException {
		ICPPASTFunctionCallExpression functionCall = ASTTools.findFirstAncestorByType(resolvingName,
				ICPPASTFunctionCallExpression.class);
		IFunction resolvedCall = FunctionCallResolver.resolveCall(functionCall, parentInstance, analyzer);

		if (resolvedCall instanceof ICPPFunctionTemplate) {
			ICPPSpecialization instantiateForFunctionCall = InstantiateForFunctionCallHelper.instantiateForFunctionCall(
					(ICPPFunctionTemplate) resolvedCall, functionCall, parentInstance.getTemplateArgumentMap());
			if (instantiateForFunctionCall instanceof IFunction) {
				resolvedCall = (IFunction) instantiateForFunctionCall;
			}
		}

		return resolvedCall;
	}

	public static ICPPClassSpecialization resolveClassTemplate(IASTName resolvingName,
			AbstractResolvedNameInfo parentResolvedName, ASTAnalyzer analyzer) throws TemplatorException {
		ICPPClassSpecialization specialization = null;
		IASTName originalResolvingName = resolvingName;
		ICPPASTTemplateId id = getTemplateId(originalResolvingName, parentResolvedName, analyzer);
		if (id != null) {
			IBinding classInstance = ClassTemplateResolver.instantiateClassTemplate(id, parentResolvedName);
			if (classInstance instanceof ICPPClassSpecialization) {
				specialization = (ICPPClassSpecialization) classInstance;
			}
		}

		return specialization;
	}

	public static ICPPASTTemplateId getTemplateId(IASTName originalResolvingName, AbstractResolvedNameInfo parent,
			ASTAnalyzer analyzer) throws TemplatorException {
		ICPPASTTemplateId id = null;
		if (originalResolvingName instanceof ICPPASTTemplateId) {
			id = (ICPPASTTemplateId) originalResolvingName;
		} else {
			IBinding resolvedBinding = originalResolvingName.resolveBinding();
			if (resolvedBinding instanceof ICPPSpecialization) {
				IBinding specializedBinding = ((ICPPSpecialization) resolvedBinding).getSpecializedBinding();

				// could be inside a nested class template, so get the first parent we find where the name resolves to
				// the same
				// class template and then get the resolving name of this parent
				AbstractResolvedNameInfo currentParent = parent;
				IBinding parentSpecializedBinding;
				boolean foundTemplateId = false;
				while (parent != null) {
					if (parent instanceof AbstractTemplateInstance) {
						parentSpecializedBinding = ((AbstractTemplateInstance) parent).getBinding()
								.getSpecializedBinding();
						if (specializedBinding == parentSpecializedBinding) {
							foundTemplateId = true;
							break;
						}
					}
					currentParent = currentParent.getParent();
				}
				if (foundTemplateId && currentParent != null) {
					if (currentParent.getResolvingName() instanceof ICPPASTTemplateId) {
						id = (ICPPASTTemplateId) currentParent.getResolvingName();
					}
				}
			}
		}
		return id;

	}

	/**
	 * Consider the following code
	 *
	 * <pre>
	 * template&lt;typename T&gt; struct Foo { void start() { newTemplateParam(1, 'c'); /* &lt;double,char&gt; *&#47 }
	 *
	 * template&lt;typename F&gt; void newTemplateParam(T first, F second) {} };
	 *
	 * int main() { Foo&lt;double&gt; doubleFoo {}; doubleFoo.start(); }
	 * </pre>
	 *
	 * The call {@code newTemplateParam(1, 'c');} should have {@code<double,char>} as template argument map. But
	 * resolving the binding for the {@link IASTName} {@code newTemplateParam} results in {@code <int, char>} which is
	 * correct at first. Because if there is a function declaration {@code void newTemplateParam(int, char)}, then this
	 * one will be chosen instead of the member function template because it is a better match. So if the name resolved
	 * to a member function template we need to check if one argument depends on any class template argument and if so
	 * replace it with the chosen class template argument.
	 */
	public static void replaceClassTemplateParameters(TemplateInstance templateInfo, AbstractResolvedNameInfo parent,
			ASTAnalyzer analyzer) throws TemplatorException {
		IBinding binding = templateInfo.getBinding();
		// it can only happen with class template member functions, that after the resolving, the template argument map
		// needs to be changed
		if (binding instanceof CPPMethodInstance) {
			IASTFunctionDefinition functionDefinition = analyzer.getFunctionDefinition(binding);
			IASTFunctionDeclarator declarator = functionDefinition.getDeclarator();
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) functionDefinition
						.getDeclarator();
				ICPPASTParameterDeclaration[] parameters = functionDeclarator.getParameters();
				for (int parameterPosition = 0; parameterPosition < parameters.length; parameterPosition++) {
					ICPPASTParameterDeclaration functionParameter = parameters[parameterPosition];
					IBinding parameterBinding = functionParameter.getDeclarator().getName().resolveBinding();
					if (parameterBinding instanceof IVariable) {
						ICPPTemplateParameter correspondingTemplateParameter = getTemplateParameter(
								((IVariable) parameterBinding).getType());
						ICPPTemplateArgument argument = parent.getArgument(correspondingTemplateParameter);
						if (argument != null) {
							templateInfo.getTemplateArgumentMap().put(correspondingTemplateParameter, argument);
						}
					}
				}
			}
		}
	}

	private static ICPPTemplateParameter getTemplateParameter(IType parameterType) {
		ICPPTemplateParameter param = null;

		IType type = SemanticUtil.getUltimateType(parameterType, false);
		if (type instanceof ICPPTemplateParameter) {
			param = (ICPPTemplateParameter) type;
		}

		return param;
	}
}
