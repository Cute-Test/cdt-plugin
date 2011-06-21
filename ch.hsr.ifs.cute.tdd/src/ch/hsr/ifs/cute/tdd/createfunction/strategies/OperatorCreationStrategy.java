/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.strategies;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTOperatorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.ParameterHelper;
import ch.hsr.ifs.cute.tdd.createfunction.FunctionCreationHelper;

@SuppressWarnings("restriction")
public class OperatorCreationStrategy implements IFunctionCreationStrategy {

	private final boolean isFree;

	public OperatorCreationStrategy(boolean isFree) {
		this.isFree = isFree;
	}

	@Override
	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit, IASTNode selectedName, IASTNode owningType, String name, TextSelection selection) {
		ICPPASTFunctionDeclarator decl = new CPPASTFunctionDeclarator(new CPPASTOperatorName(("operator" + name).toCharArray())); //$NON-NLS-1$
		CPPASTBinaryExpression binex = ToggleNodeHelper.getAncestorOfType(selectedName, IASTBinaryExpression.class);
		CPPASTUnaryExpression unex = ToggleNodeHelper.getAncestorOfType(selectedName, IASTUnaryExpression.class);
		if (binex != null) {
			IASTExpression op = null;
			if (isFree) {
				op  = binex.getOperand1();
				FunctionCreationHelper.addParameterToOperator(decl, op);
			}
			op = binex.getOperand2();
			FunctionCreationHelper.addParameterToOperator(decl, op);
		} else if (unex != null){
			if (isFree) {
				IASTExpression op = unex.getOperand();
				FunctionCreationHelper.addParameterToOperator(decl, op);
			}
		}
		ICPPASTFunctionDefinition fdef = FunctionCreationHelper.createNewFunction(localunit, selection, decl);
		if (!FunctionCreationHelper.isVoid(fdef) && FunctionCreationHelper.isConstOperator(selectedName)) {
			decl.setConst(true);
		}
		if (FunctionCreationHelper.isPostfixOperator(selectedName)) {
			ParameterHelper.addEmptyIntParameter(decl);
		} else if (!decl.isConst()){
			decl.addPointerOperator(new CPPASTReferenceOperator(false));
		}
		if (isFree) {
			decl.setConst(false);
		}

		fdef.setParent(owningType);
		return fdef;
	}
}
