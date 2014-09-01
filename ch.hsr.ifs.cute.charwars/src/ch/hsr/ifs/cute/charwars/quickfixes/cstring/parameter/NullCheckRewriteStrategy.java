package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;

public class NullCheckRewriteStrategy extends RewriteStrategy {
	@Override
	protected IASTCompoundStatement getStdStringOverloadBody() {
		IASTCompoundStatement stdStringOverloadBody = ExtendedNodeFactory.factory.newCompoundStatement();
		
		IASTStatement[] nullCheckedStatements = ASTAnalyzer.getNullCheckedStatements(strName, statements);
		for(IASTStatement statement : nullCheckedStatements) {
			stdStringOverloadBody.addStatement(statement.copy(CopyStyle.withLocations));
		}
		
		return stdStringOverloadBody;
	}
	
	@Override
	public void adaptCStringOverload() {
		IASTStatement nullCheckClause = ASTAnalyzer.getNullCheckClause(strName, statements);
		IASTCompoundStatement newClause = ExtendedNodeFactory.newCompoundStatement(getStdStringFunctionCallStatement());
		ASTModifier.replace(nullCheckClause, newClause, rewrite);
	}
}
