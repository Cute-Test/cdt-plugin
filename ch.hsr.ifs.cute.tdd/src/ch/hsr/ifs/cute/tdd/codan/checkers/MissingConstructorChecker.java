/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;

public class MissingConstructorChecker extends AbstractTDDChecker {

	public static final String ERR_ID_MissingConstructorResolutionProblem = "ch.hsr.ifs.cute.tdd.codan.checkers.MissingConstructorResolutionProblem";

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		final ICElement celement = CoreModel.getDefault().create(new Path(ast.getContainingFilename()));
		if (celement != null) {
			final ICProject project = celement.getCProject();
			if (project != null) {
				ast.accept(new MissingConstructorVisitor(project, ast));
			}
		}
	}

	private final class MissingConstructorVisitor extends ASTVisitor {
		{
			shouldVisitDeclarations = true;
		}

		private MissingConstructorVisitor(ICProject project, IASTTranslationUnit ast) {
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTSimpleDeclaration && !isMemberDeclaration(declaration)) {
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
				IASTDeclSpecifier typespec = simpleDecl.getDeclSpecifier();
				final int storageClass = typespec.getStorageClass();
				if (storageClass == IASTDeclSpecifier.sc_typedef || storageClass == IASTDeclSpecifier.sc_extern) {
					return PROCESS_CONTINUE;
				}
				if (typespec instanceof IASTNamedTypeSpecifier) {
					IASTNamedTypeSpecifier namedTypespec = (IASTNamedTypeSpecifier) typespec;
					handleNamedTypeSpecifier(simpleDecl, namedTypespec);
				} else if (typespec instanceof IASTCompositeTypeSpecifier) {
					final IASTCompositeTypeSpecifier typeDefinition = (IASTCompositeTypeSpecifier) typespec;
					handleCompositeTypeSpecifier(simpleDecl, typeDefinition);
				}
			}
			return PROCESS_CONTINUE;
		}

		private void handleCompositeTypeSpecifier(IASTSimpleDeclaration simpleDecl, final IASTCompositeTypeSpecifier typeDefinition) {
			final IASTName typeName = typeDefinition.getName();
			checkAndReportUnresolvableConstructors(simpleDecl, typeName.toString());
		}

		private void handleNamedTypeSpecifier(IASTSimpleDeclaration simpleDecl, IASTNamedTypeSpecifier namedTypespec) {
			final IBinding typeBinding = namedTypespec.getName().resolveBinding();
			if (typeBinding instanceof ICPPDeferredClassInstance) { // Workaround fix, as constructors of templates cannot be resolved correctly
				return;
			}
			if (isConstructibleType(typeBinding)) {
				String typeName = ASTTypeUtil.getType((IType) typeBinding, true);
				if (!isReferenceType(typeName)) {
					typeName = stripTemplateArguments(typeName);
					checkAndReportUnresolvableConstructors(simpleDecl, typeName);
				}
			}
		}

		private boolean isReferenceType(String typeName) {
			return typeName.contains("&") || typeName.contains("*");
		}

		private String stripTemplateArguments(String typeName) {
			return typeName.replaceAll("<.*", "");
		}

		private boolean isMemberDeclaration(IASTDeclaration declaration) {
			return declaration.getParent() instanceof IASTCompositeTypeSpecifier;
		}

		private boolean isConstructibleType(IBinding typeBinding) {
			if (typeBinding instanceof IProblemBinding) {
				return false;
			}
			if (typeBinding instanceof IType) {
				IType type = (IType) typeBinding;
				return isConstructibleType(type);
			}
			return false;
		}

		private boolean isConstructibleType(IType typeBinding) {
			IType bareType = TypeHelper.windDownToRealType(typeBinding, false);
			if (bareType instanceof IEnumeration) {
				return false;
			} else if (bareType instanceof ICPPBasicType) {
				return false;
			} else if (bareType instanceof ICPPTemplateParameter) {
				return false;
			} else if (bareType instanceof ICPPUnknownMemberClass) {
				return false;
			}
			return true;
		}

		private void checkAndReportUnresolvableConstructors(IASTSimpleDeclaration simpledec, String typename) {
			for (IASTDeclarator ctorDecl : simpledec.getDeclarators()) {
				boolean hasPointerOrRefType = TddHelper.hasPointerOrRefType(ctorDecl);
				boolean isFunctionDeclarator = ctorDecl instanceof IASTFunctionDeclarator;
				if (!hasPointerOrRefType && !isFunctionDeclarator && hasCtorInitializer(ctorDecl)) {
					if (!isConstructorAvailable(ctorDecl)) {
						reportMissingConstructor(typename, ctorDecl);
					}
				}
			}
		}

		private void reportMissingConstructor(String typename, IASTDeclarator ctorDecl) {
			IASTName reportedNode = ctorDecl.getName();
			CodanArguments ca = new CodanArguments(typename, typename, ":ctor");
			reportProblem(ERR_ID_MissingConstructorResolutionProblem, reportedNode, ca.toArray());
		}

		private boolean isConstructorAvailable(IASTDeclarator ctorDecl) {
			if (!(ctorDecl instanceof IASTImplicitNameOwner)) {
				return false;
			}
			IASTImplicitNameOwner implNameOwner = (IASTImplicitNameOwner) ctorDecl;
			IASTImplicitName[] implicitNames = implNameOwner.getImplicitNames();
			return implicitNames.length > 0 && !(implicitNames[0].getBinding() instanceof IProblemBinding);
		}

		private boolean hasCtorInitializer(IASTDeclarator ctorDecl) {
			IASTInitializer initializer = ctorDecl.getInitializer();
			// FIXME: now really? method hasXY return true if the thing is null? either the method name is crap or there is a bug for sure here
			// (lfelber)
			return initializer == null || initializer instanceof ICPPASTConstructorInitializer || initializer instanceof ICPPASTInitializerList;
		}
	}
}
