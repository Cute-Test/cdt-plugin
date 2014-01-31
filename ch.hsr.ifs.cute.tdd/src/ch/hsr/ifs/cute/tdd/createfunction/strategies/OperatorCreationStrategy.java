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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTOperatorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.ParameterHelper;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;
import ch.hsr.ifs.cute.tdd.createfunction.FunctionCreationHelper;

public class OperatorCreationStrategy implements IFunctionCreationStrategy {

	private final boolean isFree;

	public OperatorCreationStrategy(boolean isFree) {
		this.isFree = isFree;
	}

	@Override
	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit, IASTNode selectedName, String name, TextSelection selection) {
		ICPPASTFunctionDeclarator decl = new CPPASTFunctionDeclarator(new CPPASTOperatorName(("operator" + name).toCharArray()));
		IASTBinaryExpression binex = TddHelper.getAncestorOfType(selectedName, IASTBinaryExpression.class);
		IASTUnaryExpression unex = TddHelper.getAncestorOfType(selectedName, IASTUnaryExpression.class);
		if (binex != null) {
			IASTExpression op = null;
			if (isFree) {
				op = binex.getOperand1();
				FunctionCreationHelper.addParameterToOperator(decl, op);
			}
			op = binex.getOperand2();
			FunctionCreationHelper.addParameterToOperator(decl, op);
		} else if (unex != null) {
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
		} else if (!decl.isConst()) {
			decl.addPointerOperator(new CPPASTReferenceOperator(false));
		}
		if (isFree) {
			decl.setConst(false);
		}

		return fdef;
	}

	@Override
	public ICPPASTCompositeTypeSpecifier getDefinitionScopeForName(IASTTranslationUnit unit, IASTName name, CRefactoringContext context) {
		return TypeHelper.getTypeDefinitionOfVariable(unit, name, context);
	}
}
