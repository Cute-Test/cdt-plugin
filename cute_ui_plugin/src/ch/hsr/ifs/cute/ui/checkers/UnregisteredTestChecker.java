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


import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

/**
 * @author Emanuel Graf IFS
 *
 */
public class UnregisteredTestChecker extends AbstractIndexAstChecker {

	public UnregisteredTestChecker() {
	}

	public void processAst(IASTTranslationUnit ast) {
		TestFunctionFinderVisitor testFunctionFinder = new TestFunctionFinderVisitor();
		RegisteredTestFunctionFinderVisitor registeredFunctionFinder = new RegisteredTestFunctionFinderVisitor();
		ast.accept(testFunctionFinder);
		ast.accept(registeredFunctionFinder);
		markUnregisteredFinctions(testFunctionFinder.getTestFunctions(), registeredFunctionFinder.getRegisteredFunctionNames());
	}

	private void markUnregisteredFinctions(List<IASTDeclaration> testFunctions,
			List<String> registeredFunctionNames) {
		for (IASTDeclaration iastDeclaration : testFunctions) {
			if(!(registeredFunctionNames.contains(getName(iastDeclaration)))) {
				reportProblem("ch.hsr.ifs.cute.unregisteredTestMarker", iastDeclaration); //$NON-NLS-1$
			}
		}
		
	}

	private String getName(IASTDeclaration iastDeclaration) {
		if (iastDeclaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) iastDeclaration;
			if(isFunctor(funcDef)) {
				IASTNode n;
				for(n = funcDef; !(n instanceof ICPPASTCompositeTypeSpecifier); n = n.getParent()) {}
				if(n != null) {
					ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) n;
					return compType.getName().toString();
				}
			}else {
				IASTName funcName = funcDef.getDeclarator().getName();
				IBinding bind = funcName.resolveBinding();
				if (bind instanceof ICPPMethod) {
					if(!(funcName instanceof ICPPASTQualifiedName)) {
						return getQualifiedName(funcDef);
					}
				}
				return funcName.toString();
			}
		}
		return null;
	}

	private String getQualifiedName(IASTFunctionDefinition funcDef) {
		IASTName funcName = funcDef.getDeclarator().getName();
		IASTNode n;
		String className = ""; //$NON-NLS-1$
		for(n = funcDef; n != null && !(n instanceof ICPPASTCompositeTypeSpecifier); n = n.getParent()) {}
		if(n != null) {
			ICPPASTCompositeTypeSpecifier struct = (ICPPASTCompositeTypeSpecifier)n;
			className = struct.getName().toString();
		}
		return className + "::" + funcName.toString(); //$NON-NLS-1$
	}

	protected boolean isFunctor(IASTFunctionDefinition funcDef) {
		return funcDef.getDeclarator().getName() instanceof ICPPASTOperatorName;
	}

}
