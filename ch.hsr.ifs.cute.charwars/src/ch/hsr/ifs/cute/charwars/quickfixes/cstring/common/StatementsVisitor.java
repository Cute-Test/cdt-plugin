package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class StatementsVisitor extends ASTVisitor  {
	private List<IASTStatement> statements;
	
	public StatementsVisitor() {
		this.shouldVisitStatements = true;
		this.statements = new ArrayList<IASTStatement>();
	}
	

	@Override
	public int visit(IASTStatement statement) {
		if(!(statement instanceof IASTCompoundStatement)) {
			statements.add(statement);
		}
		return PROCESS_CONTINUE;
	}
	
	public IASTStatement[] getStatements() {
		return statements.toArray(new IASTStatement[]{});
	}
}