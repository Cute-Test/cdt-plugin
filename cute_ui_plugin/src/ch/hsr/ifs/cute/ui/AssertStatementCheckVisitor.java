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

class AssertStatementCheckVisitor extends ASTVisitor{
	
	@SuppressWarnings("nls")
	private static Set<String> asserts = new HashSet<String>(Arrays.asList(
			"ASSERT", "ASSERTM", 
			"FAIL", "FAILM",
			"ASSERT_EQUAL", "ASSERT_EQUALM",
			"ASSERT_EQUAL_DELTAM", "ASSERT_EQUAL_DELTA",
			"ASSERT_THROWS", "ASSERT_THROWSM"));
	
	boolean hasAssertStmt = false;
	
	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (!(statement instanceof IASTCompoundStatement)) {
			IASTNodeLocation[] locs = statement.getNodeLocations();
			if(locs.length > 1) {
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
		if(asserts.contains(name)) {
			return true;
		}
		return false;
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