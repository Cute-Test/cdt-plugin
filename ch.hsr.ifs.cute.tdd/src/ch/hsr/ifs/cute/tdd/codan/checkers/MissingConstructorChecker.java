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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;

public class MissingConstructorChecker extends AbstractTDDChecker {

	public static final String ERR_ID_MissingConstructorResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MissingConstructorResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	protected void runChecker(IASTTranslationUnit ast) {
		final ICProject project = CoreModel.getDefault().create(new Path(ast.getContainingFilename())).getCProject();
		((CIndex)ast.getIndex()).getPrimaryFragments();
		ast.accept(new MissingConstructorVisitor(project, ast));
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
				
				if (typespec instanceof IASTNamedTypeSpecifier && typespec.getStorageClass() != IASTDeclSpecifier.sc_typedef) {
					IASTNamedTypeSpecifier namedTypespec = (IASTNamedTypeSpecifier) typespec;
					IBinding typeBinding = namedTypespec.getName().resolveBinding();
					
					if (isConstructableType(typeBinding)) {
						String typeName = ASTTypeUtil.getType((IType) typeBinding, true);
						if (!(typeName.contains("&") || typeName.contains("*"))) {
							typeName = stripTemplateArguments(typeName);
							reportUnresolvableConstructorCalls(simpleDecl, typeName);
						}
					}
				}
				else if(typespec instanceof IASTCompositeTypeSpecifier){
					IASTName typeName = ((IASTCompositeTypeSpecifier) typespec).getName();
					reportUnresolvableConstructorCalls(simpleDecl, typeName.toString());
				}
			}
			return PROCESS_CONTINUE;
		}

		private String stripTemplateArguments(String typeName) {
			return typeName.replaceAll("<.*", "");
		}

		private boolean isMemberDeclaration(IASTDeclaration declaration) {
			return declaration.getParent() instanceof IASTCompositeTypeSpecifier;
		}

		private boolean isConstructableType(IBinding typeBinding) {
			if(typeBinding instanceof IProblemBinding){
				return false;
			}
			if(typeBinding instanceof IType){
				IType type = (IType) typeBinding;
				IType bareType = TypeHelper.windDownToRealType(type, false);
				if(bareType instanceof IEnumeration){
					return false;
				} else if(bareType instanceof ICPPBasicType){
					return false;
				}
				else if(bareType instanceof ICPPTemplateParameter){
					return false;
				}
				return true;
			}
			return false;
		}


		private void reportUnresolvableConstructorCalls(IASTSimpleDeclaration simpledec, String typename) {
			for (IASTDeclarator ctorDecl : simpledec.getDeclarators()) {
				boolean hasPointerOrRefType = TddHelper.hasPointerOrRefType(ctorDecl);
				if (!hasPointerOrRefType && ctorDecl instanceof IASTImplicitNameOwner && !(ctorDecl instanceof IASTFunctionDeclarator) && hasCtorInitializer(ctorDecl)) {
					IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) ctorDecl).getImplicitNames();
					if (!isConstructorAvailable(implicitNames)) {
						IASTName reportedNode = ctorDecl.getName();
						String message = Messages.MissingConstructorChecker_1 + typename;
						CodanArguments ca = new CodanArguments(typename, message, ":ctor"); //$NON-NLS-1$
						reportProblem(ERR_ID_MissingConstructorResolutionProblem_HSR, reportedNode, ca.toArray());
					}
				}
			}
		}

		private boolean isConstructorAvailable(IASTImplicitName[] implicitNames) {
			if( implicitNames.length < 1){
				return false;
			}
			//TODO: Only required as long as CDT Bug 359376 has not been solved
			else {
				IBinding ctorBinding = implicitNames[0].resolveBinding();
				if(ctorBinding instanceof IIndexFragmentBinding){
					try {
						if(((IIndexFragmentBinding) ctorBinding).hasDeclaration()){
							return true;
						}
					} catch (CoreException e) {
						return false;
					}
				}
				IBinding owner = ctorBinding.getOwner();
				if(owner instanceof ICPPClassType){
					for(ICPPConstructor ctor :((ICPPClassType) owner).getConstructors()){
						if(ctor instanceof IIndexFragmentBinding){
							try {
								if(((IIndexFragmentBinding) ctor).hasDeclaration()){
									return false;
								}
							} catch (CoreException e) {
							}
						}
					}
				}
			}
			return true;
		}

		
		private boolean hasCtorInitializer(IASTDeclarator ctorDecl) {
			IASTInitializer initializer = ctorDecl.getInitializer();
			return initializer == null || initializer instanceof ICPPASTConstructorInitializer;
		}
	}
}
