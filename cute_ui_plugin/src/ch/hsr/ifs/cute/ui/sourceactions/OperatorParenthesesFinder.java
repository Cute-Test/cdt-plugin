/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;

public class OperatorParenthesesFinder extends ASTVisitor {
	ArrayList<IASTName> al = new ArrayList<IASTName>();

	{
		shouldVisitNames = true;
	}

	@Override
	public int leave(IASTName name) {
		if (name instanceof ICPPASTOperatorName) {
			ICPPASTOperatorName opName = (ICPPASTOperatorName) name;
			if ("operator ()".equals(opName.getLastName().toString())) {
				if (name.getParent() instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator fdeclarator = (ICPPASTFunctionDeclarator) name.getParent();
					IASTParameterDeclaration fpara[] = fdeclarator.getParameters();
					if (!fdeclarator.takesVarArgs() && fpara.length == 0)
						al.add(name);
				}
			}
		}
		return super.leave(name);
	}

	public ArrayList<IASTName> getAL() {
		return al;
	}
}
