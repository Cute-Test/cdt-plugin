/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.index.IIndex;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * 
 */
public class RegisteredTestFunctionFinderVisitor extends ASTVisitor {

	{
		shouldVisitDeclarations = true;
	}

	private final List<IBinding> registeredTests;
	private final IIndex index;

	public RegisteredTestFunctionFinderVisitor(IIndex iIndex) {
		registeredTests = new ArrayList<IBinding>();
		this.index = iIndex;
	}

	public List<IBinding> getRegisteredFunctionNames() {
		return registeredTests;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declaration;
			if (simpDecl.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
				ICPPASTNamedTypeSpecifier nameDeclSpec = (ICPPASTNamedTypeSpecifier) simpDecl.getDeclSpecifier();
				String typename = nameDeclSpec.getName().toString();
				if (typename.equals("cute::suite")||typename.equals("suite")) { //$NON-NLS-1$ // last part is "suite"
//					IASTName[] suitedef = simpDecl.getTranslationUnit().getDefinitionsInAST(nameDeclSpec.getName().resolveBinding());
//					ICPPASTNamespaceDefinition ns = TddHelper.getAncestorOfType(suitedef[0], ICPPASTNamespaceDefinition.class);
//					if (ns != null && ns.getName().toString().equals("cute")) {
//					// suitedef[0] is in namespace "cute"
					IASTName suiteName = simpDecl.getDeclarators()[0].getName();
					IBinding suiteBinding = suiteName.resolveBinding();
					IASTName[] suiteRefs = suiteName.getTranslationUnit().getReferences(suiteBinding);
					for (IASTName ref : suiteRefs) {
						if (isPushBack(ref)) {
							registeredTests.add(index.adaptBinding(getRegisteredFunctionBinding(ref)));
						}
					}
//					}
				}
			}
		}
		return super.visit(declaration);
	}

	private IBinding getRegisteredFunctionBinding(IASTName ref) {
		final IASTFunctionCallExpression funcCall = getFunctionCallParent(ref);
		final IASTInitializerClause[] arguments = funcCall.getArguments();
		if (isFunctionPushBack(arguments)) {
			return getFunction(arguments);
		}
		if (isSimpleMemberFunctionPushBack(arguments)) {
			return getFunctionAtArgument(arguments, 0);
		}
		if (isMemberFunctionPushBack(arguments)) {
			return getFunctionAtArgument(arguments, 1);
		}
		if (isMemberFunctionWithContextPushBack(arguments)) {
			return getFunctionAtArgument(arguments, 1);
		}
		if (isFunctorPushBack(arguments)) {
			return getFunctor(arguments);
		}
		return null;
	}

	private IBinding getFunctor(IASTInitializerClause[] arguments) {
		if (isFunctorPushBack(arguments)) {
			final ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			final IASTIdExpression idExp = (IASTIdExpression) funcCall.getFunctionNameExpression();
			final IBinding expressionNameBinding = idExp.getName().resolveBinding();
			if (expressionNameBinding instanceof ICPPConstructor) {
				final ICPPConstructor constructorBinding = (ICPPConstructor) expressionNameBinding;
				return constructorBinding.getClassOwner();
			} else if (expressionNameBinding instanceof ICPPClassType) {
				return expressionNameBinding;
			}
		}
		return null;
	}

	private boolean isFunctorPushBack(IASTInitializerClause[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			return funcCall.getArguments().length == 0;
		}
		return false;
	}

	private IBinding getFunctionAtArgument(IASTInitializerClause[] arguments, int innerArgumentNumber) {
		if (arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			if (funcCall.getArguments().length > innerArgumentNumber && funcCall.getArguments()[innerArgumentNumber] instanceof IASTUnaryExpression) {
				IASTUnaryExpression unExp = (IASTUnaryExpression) funcCall.getArguments()[innerArgumentNumber];
				if (unExp.getOperand() instanceof IASTIdExpression) {
					IASTIdExpression idExp = (IASTIdExpression) unExp.getOperand();
					return idExp.getName().resolveBinding();
				}
			}
		}
		return null;
	}

	private boolean isSimpleMemberFunctionPushBack(IASTInitializerClause[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			return functionNameIs(funcCall, "cute::makeSimpleMemberFunctionTest"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isMemberFunctionPushBack(IASTInitializerClause[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			return functionNameIs(funcCall, "cute::makeMemberFunctionTest"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isMemberFunctionWithContextPushBack(IASTInitializerClause[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			return functionNameIs(funcCall, "cute::makeMemberFunctionTestWithContext"); //$NON-NLS-1$
		}
		return false;
	}

	private IBinding getFunction(IASTInitializerClause[] arguments) {
		if (isFunctionPushBack(arguments)) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			if (funcCall.getArguments().length == 2 && funcCall.getArguments()[0] instanceof IASTUnaryExpression) {
				IASTUnaryExpression unExp = (IASTUnaryExpression) funcCall.getArguments()[0];
				if (unExp.getOperand() instanceof IASTUnaryExpression && ((IASTUnaryExpression) unExp.getOperand()).getOperand() instanceof IASTIdExpression) {
					IASTIdExpression idExp = (IASTIdExpression) ((IASTUnaryExpression) unExp.getOperand()).getOperand();
					return idExp.getName().resolveBinding();
				}
			}
		}
		return null;
	}

	private boolean isFunctionPushBack(IASTInitializerClause[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
			return functionNameIs(funcCall, "cute::test"); //$NON-NLS-1$
		}
		return false;
	}

	protected boolean functionNameIs(ICPPASTFunctionCallExpression funcCall, String methodName) {
		if (funcCall.getFunctionNameExpression() instanceof IASTIdExpression
				&& ((IASTIdExpression) funcCall.getFunctionNameExpression()).getName().toString().startsWith(methodName)) {
			return true;
		}
		return false;
	}

	private boolean isPushBack(IASTName ref) {
		IASTFunctionCallExpression funcCall = getFunctionCallParent(ref);
		if (funcCall != null) {
			if (funcCall.getFunctionNameExpression() instanceof IASTFieldReference) {
				IASTFieldReference idExp = (IASTFieldReference) funcCall.getFunctionNameExpression();
				if (idExp.getFieldName().toString().equals("push_back")) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}

	private IASTFunctionCallExpression getFunctionCallParent(IASTName ref) {
		IASTNode n;
		for (n = ref; n != null && !(n instanceof IASTFunctionCallExpression); n = n.getParent()) {
		}
		return (IASTFunctionCallExpression) n;
	}

}
