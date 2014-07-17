/*******************************************************************************
 * Copyright (c) 2011-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.strategies;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.ParameterHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;
import ch.hsr.ifs.cute.tdd.createfunction.FunctionCreationHelper;

public abstract class OperatorCreationStrategy implements IFunctionCreationStrategy {
	@Override
	public ICPPASTCompositeTypeSpecifier getDefinitionScopeForName(IASTTranslationUnit unit, IASTName name, CRefactoringContext context) {
		return TypeHelper.getTypeDefinitionOfVariable(unit, name, context);
	}

	protected ICPPASTFunctionDefinition createFunctionDefinition(IASTTranslationUnit localunit, IASTNode selectedName,
			TextSelection selection, ICPPASTFunctionDeclarator decl) {
		ICPPASTFunctionDefinition fdef = FunctionCreationHelper.createNewFunction(localunit, selection, decl);
		if (!FunctionCreationHelper.isVoid(fdef) && FunctionCreationHelper.isConstOperator(selectedName)) {
			decl.setConst(true);
		}
		if (FunctionCreationHelper.isPostfixOperator(selectedName)) {
			ParameterHelper.addEmptyIntParameter(decl);
		} else if (!decl.isConst()) {
			decl.addPointerOperator(new CPPASTReferenceOperator(false));
		}
		return fdef;
	}
}
