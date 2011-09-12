/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.strategies;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.jface.text.TextSelection;

public class StaticFunctionCreationStrategy extends FunctionCreationStrategy {

	@Override
	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit,
			IASTNode selectedName, String name,
			TextSelection selection) {
		ICPPASTFunctionDefinition function = super.getFunctionDefinition(localunit, selectedName, name, selection);
		IASTDeclSpecifier declSpecifier = function.getDeclSpecifier();
		declSpecifier.setStorageClass(IASTDeclSpecifier.sc_static);
		return function;
	}
}
