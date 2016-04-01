package ch.hsr.ifs.templator.plugin.asttools.resolving;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import ch.hsr.ifs.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.templator.plugin.asttools.resolving.nametype.TypeNameToType;
import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.logger.TemplatorLogger;
import ch.hsr.ifs.templator.plugin.util.ReflectionMethodHelper;

public final class ClassTemplateResolver {
	private static Method isClassTemplateMethod;
	private static Method addDefaultArgumentsMethod;
	private static Method createAliasTemplaceInstanceMethod;
	private static Method argsAreTrivialMethod;
	private static Method findPartialSpecializationMethod;
	private static Method instantiateMethod;
	private static Method postResolution;

	private ClassTemplateResolver() {
	}

	static {
		try {
			isClassTemplateMethod = ReflectionMethodHelper.getNonAccessibleMethod(CPPTemplates.class, "isClassTemplate",
					ICPPASTTemplateId.class);
			addDefaultArgumentsMethod = ReflectionMethodHelper.getNonAccessibleMethod(CPPTemplates.class,
					"addDefaultArguments", ICPPTemplateDefinition.class, ICPPTemplateArgument[].class, IASTNode.class);
			createAliasTemplaceInstanceMethod = ReflectionMethodHelper.getNonAccessibleMethod(CPPTemplates.class,
					"createAliasTemplaceInstance", ICPPAliasTemplate.class, ICPPTemplateArgument[].class,
					ICPPTemplateParameterMap.class, IType.class, IBinding.class, ICPPASTTemplateId.class);
			argsAreTrivialMethod = ReflectionMethodHelper.getNonAccessibleMethod(CPPTemplates.class, "argsAreTrivial",
					ICPPTemplateParameter[].class, ICPPTemplateArgument[].class);
			findPartialSpecializationMethod = ReflectionMethodHelper.getNonAccessibleMethod(CPPTemplates.class,
					"findPartialSpecialization", ICPPClassTemplate.class, ICPPTemplateArgument[].class);
			instantiateMethod = ReflectionMethodHelper.getNonAccessibleMethod(CPPTemplates.class, "instantiate",
					ICPPClassTemplate.class, ICPPTemplateArgument[].class, boolean.class, boolean.class,
					IASTNode.class);

			postResolution = ReflectionMethodHelper.getNonAccessibleMethod(CPPSemantics.class, "postResolution",
					IBinding.class, IASTName.class);
		} catch (Exception e) {
			TemplatorLogger.errorDialogWithStackTrace(
					"Class Templates that depend on other template arguments cannot be deduced.",
					"Methods in CPPTemplates are called via reflection to resolve class templates and their definition changed.",
					e);
		}

	}

	public static IBinding instantiateClassTemplate(ICPPASTTemplateId id, AbstractResolvedNameInfo parent)
			throws TemplatorException {
		try {
			Boolean isClassTemplate = ReflectionMethodHelper.<Boolean> invokeStaticMethod(isClassTemplateMethod, id);
			if (!isClassTemplate) {

				// Functions are instantiated as part of the resolution process.
				IBinding result = CPPVisitor.createBinding(id);
				IASTName templateName = id.getTemplateName();
				if (result instanceof ICPPClassTemplate) {
					templateName.setBinding(result);
					id.setBinding(null);
				} else {
					if (result instanceof ICPPTemplateInstance) {
						templateName.setBinding(((ICPPTemplateInstance) result).getTemplateDefinition());
					} else {
						templateName.setBinding(result);
					}
					return result;
				}
			}

			IASTNode parentOfName = id.getParent();
			boolean isLastName = true;
			if (parentOfName instanceof ICPPASTQualifiedName) {
				isLastName = ((ICPPASTQualifiedName) parentOfName).getLastName() == id;
				parentOfName = parentOfName.getParent();
			}

			boolean isDeclaration = false;
			boolean isDefinition = false;
			boolean isExplicitSpecialization = false;
			if (isLastName && parentOfName != null) {
				IASTNode declaration = parentOfName.getParent();
				if (declaration instanceof IASTSimpleDeclaration) {
					if (parentOfName instanceof ICPPASTElaboratedTypeSpecifier) {
						isDeclaration = true;
					} else if (parentOfName instanceof ICPPASTCompositeTypeSpecifier) {
						isDefinition = true;
					}
					if (isDeclaration || isDefinition) {
						IASTNode parentOfDeclaration = declaration.getParent();
						if (parentOfDeclaration instanceof ICPPASTExplicitTemplateInstantiation) {
							isDeclaration = false;
						} else if (parentOfDeclaration instanceof ICPPASTTemplateSpecialization) {
							isExplicitSpecialization = true;
						}
					}
				}
			}

			IBinding result = null;
			IASTName templateName = id.getTemplateName();
			IBinding template = templateName.resolvePreBinding();

			// Alias template.
			if (template instanceof ICPPAliasTemplate) {
				ICPPAliasTemplate aliasTemplate = (ICPPAliasTemplate) template;
				ICPPTemplateArgument[] args = CPPTemplates.createTemplateArgumentArray(id);
				args = addDefaultArguments(aliasTemplate, args, id);
				if (args == null) {
					return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS,
							templateName.toCharArray());
				}
				ICPPTemplateParameterMap parameterMap = CPPTemplates.createParameterMap(aliasTemplate, args);
				IType aliasedType = aliasTemplate.getType();
				IBinding owner = template.getOwner();
				return ReflectionMethodHelper.<IBinding> invokeStaticMethod(createAliasTemplaceInstanceMethod,
						aliasTemplate, args, parameterMap, aliasedType, owner, id);
			}

			// Alias template instance.
			if (template instanceof ICPPAliasTemplateInstance) {
				ICPPAliasTemplateInstance aliasTemplateInstance = (ICPPAliasTemplateInstance) template;
				ICPPTemplateArgument[] args = CPPTemplates.createTemplateArgumentArray(id);
				ICPPAliasTemplate aliasTemplate = aliasTemplateInstance.getTemplateDefinition();
				args = addDefaultArguments(aliasTemplate, args, id);
				if (args == null) {
					return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS,
							templateName.toCharArray());
				}
				ICPPTemplateParameterMap parameterMap = CPPTemplates.createParameterMap(aliasTemplate, args);
				IType aliasedType = aliasTemplateInstance.getType();
				IBinding owner = aliasTemplateInstance.getOwner();
				return ReflectionMethodHelper.<IBinding> invokeStaticMethod(createAliasTemplaceInstanceMethod,
						aliasTemplate, args, parameterMap, aliasedType, owner, id);
			}

			// Class template.
			if (template instanceof ICPPConstructor) {
				template = template.getOwner();
			}

			if (template instanceof ICPPUnknownMemberClass) {
				IType owner = ((ICPPUnknownMemberClass) template).getOwnerType();
				ICPPTemplateArgument[] args = CPPTemplates.createTemplateArgumentArray(id);
				args = SemanticUtil.getSimplifiedArguments(args);
				return new CPPUnknownClassInstance(owner, id.getSimpleID(), args);
			}

			if (!(template instanceof ICPPClassTemplate)
					|| template instanceof ICPPClassTemplatePartialSpecialization) {
				return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE, templateName.toCharArray());
			}

			final ICPPClassTemplate classTemplate = (ICPPClassTemplate) template;
			ICPPTemplateArgument[] args = CPPTemplates.createTemplateArgumentArray(id);
			if (CPPTemplates.hasDependentArgument(args)) {
				ICPPASTTemplateDeclaration tdecl = CPPTemplates.getTemplateDeclaration(id);
				if (tdecl != null) {
					Boolean argsAreTrivial = ReflectionMethodHelper.<Boolean> invokeStaticMethod(argsAreTrivialMethod,
							classTemplate.getTemplateParameters(), args);
					if (argsAreTrivial) {
						result = classTemplate;
					} else {
						args = addDefaultArguments(classTemplate, args, id);
						if (args == null) {
							return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS,
									templateName.toCharArray());
						}
						ICPPClassTemplatePartialSpecialization partialSpec = ReflectionMethodHelper
								.<ICPPClassTemplatePartialSpecialization> invokeStaticMethod(
										findPartialSpecializationMethod, classTemplate, args);
						if (isDeclaration || isDefinition) {
							if (partialSpec == null) {
								partialSpec = new CPPClassTemplatePartialSpecialization(id, args);
								if (template instanceof ICPPInternalClassTemplate) {
									((ICPPInternalClassTemplate) template).addPartialSpecialization(partialSpec);
								}
								return partialSpec;
							}
						}
						if (partialSpec == null) {
							return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE,
									templateName.toCharArray());
						}
						result = partialSpec;
					}
				} else {
					replaceDependentTemplateArguments(id, parent, args);
				}
			}
			if (result == null) {
				result = ReflectionMethodHelper.<IBinding> invokeStaticMethod(instantiateMethod, classTemplate, args,
						isDefinition, isExplicitSpecialization, id);
				if (result instanceof ICPPInternalBinding) {
					if (isDeclaration) {
						ASTInternal.addDeclaration(result, id);
					} else if (isDefinition) {
						ASTInternal.addDefinition(result, id);
					}
				}
			}
			return ReflectionMethodHelper.<IBinding> invokeStaticMethod(postResolution, result, id);
		} catch (DOMException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| ClassCastException e) {
			return null;
		}
	}

	private static void replaceDependentTemplateArguments(ICPPASTTemplateId id, AbstractResolvedNameInfo parent,
			ICPPTemplateArgument[] args) throws TemplatorException {
		for (int i = 0; i < args.length; i++) {
			ICPPTemplateArgument arg = args[i];
			IType typeValue = arg.getTypeValue();
			if (typeValue instanceof ICPPTemplateParameter) {
				args[i] = parent.getArgument((ICPPTemplateParameter) typeValue);
			} else if (typeValue instanceof ICPPDeferredClassInstance || typeValue instanceof ICPPUnknownMemberClass) {
				IASTNode templateArgument = id.getTemplateArguments()[i];
				if (templateArgument instanceof IASTTypeId) {
					IASTDeclSpecifier declSpecifier = ((IASTTypeId) templateArgument).getDeclSpecifier();
					if (declSpecifier instanceof IASTNamedTypeSpecifier) {
						IASTName typeName = ((IASTNamedTypeSpecifier) declSpecifier).getName();
						if (typeName instanceof ICPPASTTemplateId) {
							IBinding nestedInstance = instantiateClassTemplate((ICPPASTTemplateId) typeName, parent);
							if (nestedInstance instanceof IType) {
								args[i] = new CPPTemplateTypeArgument((IType) nestedInstance);
							}
						} else {
							TypeNameToType nestedType = parent.getAnalyzer().getType(typeName, parent);
							if (nestedType == null || nestedType.getType() == null) {
								throw new TemplatorException(
										"Could not determine argument for " + args[i] + " in " + id + ".");
							}
							args[i] = new CPPTemplateTypeArgument(nestedType.getType());
						}
					}
				}
			}
		}
	}

	public static ICPPTemplateArgument[] addDefaultArguments(ICPPTemplateDefinition template,
			ICPPTemplateArgument[] arguments, IASTNode point) throws IllegalAccessException, IllegalArgumentException,
					InvocationTargetException, ClassCastException {
		return ReflectionMethodHelper.<ICPPTemplateArgument[]> invokeStaticMethod(addDefaultArgumentsMethod, template,
				arguments, point);
	}
}