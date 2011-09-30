/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.ParameterHelper;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;

public class ConstructorCreationStrategy implements IFunctionCreationStrategy {

	@Override
	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit, IASTNode selectedName, String name,
			TextSelection selection) {

		CPPASTFunctionDeclarator funcdecl = new CPPASTFunctionDeclarator(new CPPASTName(name.toCharArray()));

		CPPASTDeclarator declarator = TddHelper.getAncestorOfType(selectedName, CPPASTDeclarator.class);
		ParameterHelper.addTo(declarator, funcdecl);

		CPPASTNamedTypeSpecifier declspec = new CPPASTNamedTypeSpecifier();
		declspec.setName(new CPPASTName());
		CPPASTFunctionDefinition fd = new CPPASTFunctionDefinition(declspec, funcdecl, new CPPASTCompoundStatement());
		return fd;

	}

	@Override
	public ICPPASTCompositeTypeSpecifier getDefinitionScopeForName(IASTTranslationUnit unit, IASTName name, RefactoringASTCache astCache) {
		return TypeHelper.getTypeDefinitionOfVariable(unit, name, astCache);
	}
}
