/*******************************************************************************
 * Copyright (c) 2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.strategies;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.createfunction.FunctionCreationHelper;

public class MemberOperatorCreationStrategy extends OperatorCreationStrategy {
	@Override
	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit, IASTNode selectedName,
			String name, ISelection selection) {
		ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		ICPPASTFunctionDeclarator decl = nodeFactory.newFunctionDeclarator(nodeFactory.newName(("operator" + name).toCharArray()));
		IASTBinaryExpression binex = TddHelper.getAncestorOfType(selectedName, IASTBinaryExpression.class);
		if (binex != null) {
			IASTExpression op = binex.getOperand2();
			FunctionCreationHelper.addParameterToOperator(decl, op);
		}
		return createFunctionDefinition(localunit, selectedName, selection, decl);
	}
}
