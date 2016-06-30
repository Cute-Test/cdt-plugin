package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class ExistingReferencesReplacer implements F1V<ExtractInterfaceContext> {
	private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

	@Override
	public void apply(ExtractInterfaceContext context) {
		if (context.shouldReplaceAllOccurences()) {
			replaceAllOccurences(context);
		}
	}

	private static void replaceAllOccurences(ExtractInterfaceContext context) {
		for (IASTName usage : getUsagesOfConcreteType(context))
			if (isPointerOrRefOrFwdDeclToChosenClass(usage, context)) {
				replaceDeclarationWithNewType(usage, context);
			}
	}

	private static Collection<IASTName> getUsagesOfConcreteType(ExtractInterfaceContext context) {
		IASTName chosenClassName = context.getChosenClass().getName();
		NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
		Collection<IASTName> usages = lookup.findReferencingNames(chosenClassName, context.getCRefContext());
		addLocalUsagesIfNecessary(context, chosenClassName, usages);
		usages.addAll(lookup.findDeclarations(chosenClassName, context.getCRefContext()));
		return usages;
	}

	private static void addLocalUsagesIfNecessary(ExtractInterfaceContext context, IASTName className,
			Collection<IASTName> usages) {
		if (!usages.isEmpty())
			return;
		IASTName[] references = context.getTuOfChosenClass().getReferences(className.resolveBinding());
		usages.addAll(list(references));
	}

	private static boolean isPointerOrRefOrFwdDeclToChosenClass(IASTName usage, ExtractInterfaceContext context) {
		if (isPartOfExpression(usage))
			return false;
		IASTNode declaration = getDeclaration(usage);
		IASTDeclarator declarator = getDeclarator(declaration, context.getChosenClass().getName());
		return AstUtil.hasPointerOrRefType(declarator) || isClassForwardDeclaration(declaration);
	}

	private static boolean isPartOfExpression(IASTName usage) {
		return AstUtil.getAncestorOfType(usage, ICPPASTExpression.class) != null;
	}

	private static IASTDeclarator getDeclarator(IASTNode declaration, IASTName className) {
		if (hasTemplateId(declaration)) {
			ICPPASTNamedTypeSpecifier namedType = getTypeSpecIfRefersToClass(declaration, className);
			return AstUtil.getChildOfType(namedType.getParent(), IASTDeclarator.class);
		}
		return AstUtil.getDeclaratorForNode(declaration);
	}

	private static boolean hasTemplateId(IASTNode node) {
		return AstUtil.getChildOfType(node, ICPPASTTemplateId.class) != null;
	}

	private static void replaceDeclarationWithNewType(IASTName name, ExtractInterfaceContext context) {
		ASTRewrite rewriter = context.getRewriterFor(name.getTranslationUnit());
		IASTNode declaration = getDeclaration(name);

		if (declaration instanceof IASTSimpleDeclaration) {
			handleSimpleDecl(context.getNewInterfaceName(), rewriter, (IASTSimpleDeclaration) declaration, name);
		} else if (declaration instanceof ICPPASTParameterDeclaration) {
			handleParameter(context.getNewInterfaceName(), rewriter, (ICPPASTParameterDeclaration) declaration, name);
		} else if (declaration instanceof ICPPASTFunctionDefinition) {
			handleReturnType(context.getNewInterfaceName(), rewriter, (ICPPASTFunctionDefinition) declaration);
		}
	}

	private static ICPPASTNamedTypeSpecifier getTypeSpecIfRefersToClass(IASTNode selectedNode, IASTName className) {
		if (referesToSameEntity(selectedNode, className))
			return (ICPPASTNamedTypeSpecifier) selectedNode;

		for (IASTNode node : selectedNode.getChildren()) {
			ICPPASTNamedTypeSpecifier namedSpec = getTypeSpecIfRefersToClass(node, className);

			if (namedSpec != null)
				return namedSpec;
		}

		return null;
	}

	private static boolean referesToSameEntity(IASTNode node, IASTName astName) {
		return node instanceof ICPPASTNamedTypeSpecifier
				&& ((ICPPASTNamedTypeSpecifier) node).getName().toString().equals(astName.toString());
	}

	private static IASTNode getDeclaration(IASTNode node) {
		while (node != null && !(node instanceof IASTSimpleDeclaration) && !(node instanceof IASTParameterDeclaration)
				&& !(node instanceof ICPPASTFunctionDefinition)) {
			node = node.getParent();
		}
		return node;
	}

	private static boolean isClassForwardDeclaration(IASTNode node) {
		if ((node instanceof ICPPASTTemplateDeclaration)) {
			node = ((ICPPASTTemplateDeclaration) node).getDeclaration();
		}

		if ((node instanceof IASTSimpleDeclaration))
			return isClassFwd(((IASTSimpleDeclaration) node).getDeclSpecifier());

		return false;
	}

	private static boolean isClassFwd(IASTDeclSpecifier specifier) {
		return specifier instanceof IASTElaboratedTypeSpecifier;
	}

	private static void handleSimpleDecl(String newInterfaceName, ASTRewrite rewriter, IASTSimpleDeclaration simpleDecl,
			IASTName astName) {
		IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();

		if (isClassFwd(declSpec)) {
			handleClassFwdDecl(newInterfaceName, rewriter, (ICPPASTElaboratedTypeSpecifier) declSpec);
		} else if (hasTemplateId(simpleDecl)) {
			handleTemplateId(newInterfaceName, rewriter, simpleDecl, astName);
		} else {
			handleNamedType(newInterfaceName, rewriter, declSpec);
		}
	}

	private static void handleNamedType(String newName, ASTRewrite r, IASTDeclSpecifier declSpec) {
		r.replace(declSpec, createNewNamedType(newName, (ICPPASTNamedTypeSpecifier) declSpec), null);
	}

	private static void handleClassFwdDecl(String newInterfaceName, ASTRewrite rewriter,
			ICPPASTElaboratedTypeSpecifier declSpecifier) {
		ICPPASTElaboratedTypeSpecifier specifier = declSpecifier.copy();
		specifier.setName(createNewInterfaceName(newInterfaceName, specifier.getName()));
		rewriter.replace(declSpecifier, specifier, null);
	}

	private static void handleReturnType(String interfaceName, ASTRewrite rewriter, ICPPASTFunctionDefinition funDef) {
		IASTDeclSpecifier declSpec = funDef.getDeclSpecifier();
		ICPPASTNamedTypeSpecifier newSpecifier = createNewNamedType(interfaceName,
				(ICPPASTNamedTypeSpecifier) declSpec);
		rewriter.replace(declSpec, newSpecifier, null);
	}

	private static void handleParameter(String newInterfaceName, ASTRewrite rewriter,
			ICPPASTParameterDeclaration paramDecl, IASTName astName) {
		if (hasTemplateId(paramDecl)) {
			handleTemplateId(newInterfaceName, rewriter, paramDecl, astName);
		} else {
			ICPPASTNamedTypeSpecifier newSpecifier = createNewNamedType(newInterfaceName,
					(ICPPASTNamedTypeSpecifier) paramDecl.getDeclSpecifier());
			rewriter.replace(paramDecl.getDeclSpecifier(), newSpecifier, null);
		}
	}

	private static void handleTemplateId(String newName, ASTRewrite rewriter, IASTNode parentDecl, IASTName astName) {
		ICPPASTTemplateId templateId = AstUtil.getChildOfType(parentDecl, ICPPASTTemplateId.class);
		IASTName templateName = templateId.getTemplateName().copy();
		ICPPASTTemplateId newTemplateId = nodeFactory.newTemplateId(templateName);

		for (IASTNode arg : templateId.getTemplateArguments()) {
			if (arg instanceof ICPPASTTypeId) {
				newTemplateId.addTemplateArgument(getType(newName, astName, arg));
			} else if (arg instanceof IASTExpression) {
				newTemplateId.addTemplateArgument((IASTExpression) arg.copy());
			}
		}

		rewriter.replace(templateId, newTemplateId, null);
	}

	private static ICPPASTTypeId getType(String newInterfaceName, IASTName astName, IASTNode templateArg) {
		ICPPASTTypeId copy = (ICPPASTTypeId) templateArg.copy();
		ICPPASTNamedTypeSpecifier classSpec = getTypeSpecIfRefersToClass(templateArg, astName);

		if (classSpec != null) {
			ICPPASTNamedTypeSpecifier newTypeSpec = createNewNamedType(newInterfaceName, classSpec);
			copy.setDeclSpecifier(newTypeSpec);
		}

		return copy;
	}

	private static ICPPASTNamedTypeSpecifier createNewNamedType(String newInterfaceName,
			ICPPASTDeclSpecifier declSpec) {
		ICPPASTNamedTypeSpecifier newNameSpec = (ICPPASTNamedTypeSpecifier) declSpec.copy();
		IASTName name = createNewInterfaceName(newInterfaceName, newNameSpec.getName());
		newNameSpec.setName(name);
		return newNameSpec;
	}

	private static IASTName createNewInterfaceName(String newInterfaceName, IASTName oldName) {
		if (AstUtil.isQualifiedName(oldName))
			return getNewQNameForInterface((ICPPASTQualifiedName) oldName, newInterfaceName);

		return nodeFactory.newName(newInterfaceName.toCharArray());
	}

	private static ICPPASTQualifiedName getNewQNameForInterface(ICPPASTQualifiedName qName, String newInterfaceName) {
		ICPPASTQualifiedName newQfName = nodeFactory
				.newQualifiedName(nodeFactory.newName(newInterfaceName.toCharArray()));
		List<ICPPASTNameSpecifier> qNameWithoutClassName = list(qName.getQualifier());

		for (ICPPASTNameSpecifier name : qNameWithoutClassName) {
			newQfName.addNameSpecifier(name);
		}

		return newQfName;
	}
}
