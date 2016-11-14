/*******************************************************************************
 * Copyright (c) 2007-2013, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

class AssertStatementCheckVisitor extends ASTVisitor {

	@SuppressWarnings("nls")
	private static Set<String> asserts = new HashSet<String>(Arrays.asList("ASSERT", "ASSERTM", "FAIL", "FAILM", "ASSERT_DDT", "ASSERT_DDTM", "ASSERT_EQUAL",
			"ASSERT_EQUALM", "ASSERT_EQUAL_DELTAM", "ASSERT_EQUAL_DELTA", "ASSERT_EQUAL_DDT", "ASSERT_EQUAL_DDTM", "ASSERT_EQUAL_DELTA_DDTM",
			"ASSERT_EQUAL_DELTA_DDT", "ASSERT_THROWS", "ASSERT_THROWSM", "ASSERT_LESS", "ASSERT_LESSM", "ASSERT_LESS_EQUAL", "ASSERT_LESS_EQUALM",
			"ASSERT_GREATER", "ASSERT_GREATERM", "ASSERT_GREATER_EQUAL", "ASSERT_GREATER_EQUALM", "ASSERT_NOT_EQUAL_TO", "ASSERT_NOT_EQUAL_TOM",
			"ASSERT_LESS_DDT", "ASSERT_LESS_DDTM", "ASSERT_LESS_EQUAL_DDT", "ASSERT_LESS_EQUAL_DDTM", "ASSERT_GREATER_DDT", "ASSERT_GREATER_DDTM",
			"ASSERT_GREATER_EQUAL_DDT", "ASSERT_GREATER_EQUAL_DDTM", "ASSERT_NOT_EQUAL_TO_DDT", "ASSERT_NOT_EQUAL_TO_DDTM", "ASSERT_EQUAL_RANGES"));

	boolean hasAssertStmt = false;

	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (!(statement instanceof IASTCompoundStatement)) {
			IASTNodeLocation[] locs = statement.getNodeLocations();
			if (locs.length > 1) {
				ArrayList<IASTMacroExpansionLocation> expLocs = getMacroExpansionLocs(locs);
				for (IASTMacroExpansionLocation expansionLocation : expLocs) {
					hasAssertStmt = isAssertExpansion(expansionLocation);
				}
			}
		}
		return super.visit(statement);
	}

	private boolean isAssertExpansion(IASTMacroExpansionLocation expansionLocation) {
		String name = expansionLocation.getExpansion().getMacroDefinition().getName().toString();
		return asserts.contains(name);
	}

	private ArrayList<IASTMacroExpansionLocation> getMacroExpansionLocs(IASTNodeLocation[] locs) {
		ArrayList<IASTMacroExpansionLocation> res = new ArrayList<IASTMacroExpansionLocation>();
		for (IASTNodeLocation iastNodeLocation : locs) {
			if (iastNodeLocation instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) iastNodeLocation;
				res.add(macroLoc);
			}
		}
		return res;
	}

}