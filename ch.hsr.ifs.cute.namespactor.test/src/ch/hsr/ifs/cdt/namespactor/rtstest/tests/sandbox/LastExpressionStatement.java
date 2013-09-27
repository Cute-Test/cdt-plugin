/******************************************************************************
* Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/
package ch.hsr.ifs.cdt.namespactor.rtstest.tests.sandbox;

/*******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDirective;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

@SuppressWarnings("restriction")
public class LastExpressionStatement extends ChangeGeneratorTest {

	public LastExpressionStatement(){
		super("Last Replace After Insert Has No Line Break"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source         = "void f()\r\n{\r\n\tint i = 0;\r\n\tf();\r\n}\r\n"; //$NON-NLS-1$
		expectedSource = "void f()\r\n{\r\n\tint i = 0;\r\n\tusing namespace std;\r\n}\r\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {		
		return new LastExpressionStatement();
	}


	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}
			
			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTExpressionStatement) {
					IASTExpressionStatement exprStatement = (IASTExpressionStatement) statement;

					ASTModification modification1 = new ASTModification(ASTModification.ModificationKind.REPLACE, exprStatement, null, null);
					ASTModification modification2 = new ASTModification(ASTModification.ModificationKind.INSERT_BEFORE, exprStatement, new CPPASTDeclarationStatement(new CPPASTUsingDirective(new CPPASTName("std".toCharArray()))), null);
					modStore.storeModification(null, modification1);
					modStore.storeModification(null, modification2);
				}

				return PROCESS_CONTINUE;
			}
		};
	}
}
