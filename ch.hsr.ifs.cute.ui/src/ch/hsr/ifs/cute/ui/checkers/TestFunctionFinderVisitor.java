/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

import ch.hsr.ifs.cute.ui.ASTUtil;

class TestFunctionFinderVisitor extends ASTVisitor {

	private final List<IASTDeclaration> testFunctions;

	TestFunctionFinderVisitor() {
		testFunctions = new ArrayList<IASTDeclaration>();
	}

	{
		shouldVisitDeclarations = true;
	}

	public List<IASTDeclaration> getTestFunctions() {
		return testFunctions;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if (ASTUtil.isTestFunction(declaration)) {
			testFunctions.add(declaration);
		}
		return super.visit(declaration);
	}

}