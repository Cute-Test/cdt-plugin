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
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.jface.text.TextSelection;

import ch.hsr.ifs.cute.tdd.ParameterHelper;

@SuppressWarnings("restriction")
public class ConstructorCreationStrategy implements IFunctionCreationStrategy{

	@Override
	public ICPPASTFunctionDefinition getFunctionDefinition(IASTTranslationUnit localunit,
			IASTNode selectedName, IASTNode owningType, String name,
			TextSelection selection) {
		//FIXME: maybe not needed, why is this here called with owningtype not compositetype
		if (owningType instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier ctype = (ICPPASTCompositeTypeSpecifier) owningType;
			IASTName newFuncDeclName = ctype.getName().copy();
			CPPASTFunctionDeclarator funcdecl = new CPPASTFunctionDeclarator(newFuncDeclName);
		
			CPPASTDeclarator declarator = ToggleNodeHelper.getAncestorOfType(selectedName, CPPASTDeclarator.class);
			ParameterHelper.addTo(declarator, funcdecl);
			
			CPPASTNamedTypeSpecifier declspec = new CPPASTNamedTypeSpecifier();
			declspec.setName(new CPPASTName());
			CPPASTFunctionDefinition fd = new CPPASTFunctionDefinition(declspec, funcdecl, new CPPASTCompoundStatement());
			fd.setParent(owningType);
			return fd;
		}
		return null;
	}

}