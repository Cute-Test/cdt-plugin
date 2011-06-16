/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.addArgument;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;

public class NameCollector extends ASTVisitor {

	private ArrayList<IASTName> result = new ArrayList<IASTName>();
	{
		shouldVisitNames = true;
	}

	@Override
	public int visit(IASTName name) {
		result.add(name);
		return PROCESS_CONTINUE;
	}

	public IASTName getFirstName() {
		if (result.size() > 0) {
			return (IASTName) result.get(0);
		}
		return null;
	}

	public ArrayList<IASTName> getNames() {
		return result;
	}

}
