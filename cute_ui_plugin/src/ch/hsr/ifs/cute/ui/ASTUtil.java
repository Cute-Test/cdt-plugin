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
package ch.hsr.ifs.cute.ui;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ASTUtil {
	
	public static boolean isTestFunction(IASTDeclaration declaration) {
		if (declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) declaration;
			if(hasNoParameters(funcDef)) {
				if(containsAssert(funcDef)) {
					return true;
				}
			}
		}
		return false;				
	}

	public static boolean containsAssert(IASTFunctionDefinition funcDef) {
		AssertStatementCheckVisitor checker = new AssertStatementCheckVisitor();
		funcDef.getBody().accept(checker);
		return checker.hasAssertStmt;
	}

	public static boolean hasNoParameters(IASTFunctionDefinition funcDef) {
		IASTFunctionDeclarator declarator = funcDef.getDeclarator();
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator stdFuncDecl = (IASTStandardFunctionDeclarator) declarator;
			if(stdFuncDecl.getParameters() == null || stdFuncDecl.getParameters().length == 0) {
				return true;
			}
		}
		return false;
	}

}
