/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

/**
 * @author Emanuel Graf IFS
 *
 */
public class RegisteredTestFunctionFinderVisitor extends ASTVisitor {
	
	{
		shouldVisitStatements = true;
	}
	
	private List<String> registeredTests;

	public RegisteredTestFunctionFinderVisitor() {
		registeredTests = new ArrayList<String>();
	}
	
	public List<String> getRegisteredFunctionNames(){
		return registeredTests;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
			if (declStmt.getDeclaration() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declStmt.getDeclaration();
				if (simpDecl.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
					ICPPASTNamedTypeSpecifier nameDeclSpec = (ICPPASTNamedTypeSpecifier) simpDecl.getDeclSpecifier();
					if(nameDeclSpec.getName().toString().equals("cute::suite")) {
						IASTName suiteName = simpDecl.getDeclarators()[0].getName();
						IBinding suiteBinding = suiteName.resolveBinding();
						IASTName[] suiteRefs = suiteName.getTranslationUnit().getReferences(suiteBinding);
						for (IASTName ref : suiteRefs) {
							if(isPushBack(ref)) {
								registeredTests.add(getRegisteredFunctionName(ref));
							}
						}
					}
				}
			}
		}
		return super.visit(statement);
	}

	private String getRegisteredFunctionName(IASTName ref) {
		IASTFunctionCallExpression funcCall = getFunctionCallParent(ref);
		IASTInitializerClause[] arguments = funcCall.getArguments();
		if(isFunctionPushBack(arguments)){
			return getFunctionName(arguments);
		}
		if(isSimpleMemberFunctionPushBack(arguments)) {
			return getSimpleMemberFunctionName(arguments);
		}
		//TODO handle other Member push_backs
		if(isFunctorPushBack(arguments)) {
			return getFunctorName(arguments);
		}
		return "";
	}

	private String getFunctorName(IASTInitializerClause[] arguments) {
		if(isFunctorPushBack(arguments)) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression)arguments[0];
			IASTIdExpression idExp = (IASTIdExpression)funcCall.getFunctionNameExpression();
			return idExp.getName().toString();
		}
		return "";
	}

	private boolean isFunctorPushBack(IASTInitializerClause[] arguments) {
		if(arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression)arguments[0];
			if(funcCall.getArguments().length == 0) {
				return true;
			}
		}
		return false;
	}

	private String getSimpleMemberFunctionName(IASTInitializerClause[] arguments) {
		if(isSimpleMemberFunctionPushBack(arguments)) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression)arguments[0];
			if(funcCall.getArguments().length == 2 && funcCall.getArguments()[0] instanceof IASTUnaryExpression) {
				IASTUnaryExpression unExp = (IASTUnaryExpression)funcCall.getArguments()[0];
				if (unExp.getOperand() instanceof IASTIdExpression) {
					IASTIdExpression idExp = (IASTIdExpression) unExp.getOperand();
					return idExp.getName().toString();
				}
			}
		}
		return "";
	}

	private boolean isSimpleMemberFunctionPushBack(IASTInitializerClause[] arguments) {
		if(arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression)arguments[0];
			return functionNameIs(funcCall, "cute::makeSimpleMemberFunctionTest");
		}
		return false;
	}

	private String getFunctionName(IASTInitializerClause[] arguments) {
		if(isFunctionPushBack(arguments)) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression)arguments[0];
			if(funcCall.getArguments().length == 2 && funcCall.getArguments()[0] instanceof IASTUnaryExpression) {
				IASTUnaryExpression unExp = (IASTUnaryExpression)funcCall.getArguments()[0];
				if (unExp.getOperand() instanceof IASTUnaryExpression && ((IASTUnaryExpression)unExp.getOperand()).getOperand() instanceof IASTIdExpression) {
					IASTIdExpression idExp = (IASTIdExpression) ((IASTUnaryExpression)unExp.getOperand()).getOperand();
					return idExp.getName().toString();
				}
			}
		}
		return "";
	}

	private boolean isFunctionPushBack(IASTInitializerClause[] arguments) {
		if(arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression)arguments[0];
			return functionNameIs(funcCall, "cute::test");
		}
		return false;
	}

	protected boolean functionNameIs(ICPPASTFunctionCallExpression funcCall, String methodName) {
		if(funcCall.getFunctionNameExpression() instanceof IASTIdExpression && 
				((IASTIdExpression)funcCall.getFunctionNameExpression()).getName().toString().startsWith(methodName)) {
			return true;
		}
		return false;
	}

	private boolean isPushBack(IASTName ref) {
		IASTFunctionCallExpression funcCall = getFunctionCallParent(ref);
		if(funcCall != null) {
			if (funcCall.getFunctionNameExpression() instanceof IASTFieldReference) {
				IASTFieldReference idExp = (IASTFieldReference) funcCall.getFunctionNameExpression();
				if(idExp.getFieldName().toString().equals("push_back")){
					return true;
				}
			}
		}
		return false;
	}

	private IASTFunctionCallExpression getFunctionCallParent(IASTName ref) {
		IASTNode n;
		for(n = ref;n != null && !(n instanceof IASTFunctionCallExpression); n = n.getParent()) {}
		return (IASTFunctionCallExpression)n;
	}
	
	

}