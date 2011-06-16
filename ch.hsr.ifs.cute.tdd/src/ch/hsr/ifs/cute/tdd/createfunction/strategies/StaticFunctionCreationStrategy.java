/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.strategies;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.jface.text.TextSelection;

public class StaticFunctionCreationStrategy extends NormalFunctionCreationStrategy {

	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit,
			IASTNode selectedName, IASTNode owningType, String name,
			TextSelection selection) {
		ICPPASTFunctionDefinition function = super.getFunctionDefinition(localunit, selectedName, owningType, name, selection);
		ICPPASTSimpleDeclSpecifier simpledec = (ICPPASTSimpleDeclSpecifier) function.getDeclSpecifier();
		simpledec.setStorageClass(ICPPASTSimpleDeclSpecifier.sc_static);
		return function;
	}

}
