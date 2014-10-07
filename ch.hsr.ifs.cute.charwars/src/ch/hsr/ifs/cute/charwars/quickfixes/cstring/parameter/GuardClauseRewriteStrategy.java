package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

public class GuardClauseRewriteStrategy extends RewriteStrategy {
	@Override
	protected IASTCompoundStatement getStdStringOverloadBody() {
		IASTCompoundStatement stdStringOverloadBody = ExtendedNodeFactory.newCompoundStatement();
		IASTStatement[] nullCheckedStatements = CheckAnalyzer.getNullCheckedStatements(strName, statements);
		for(IASTStatement statement : nullCheckedStatements) {
			stdStringOverloadBody.addStatement(statement.copy(CopyStyle.withLocations));
		}
		return stdStringOverloadBody;
	}
	
	@Override
	public void adaptCStringOverload() {
		IASTStatement[] nullCheckedStatements = CheckAnalyzer.getNullCheckedStatements(strName, statements);
		IASTStatement insertionPoint = nullCheckedStatements[0];
		ASTModifier.insertBefore(insertionPoint.getParent(), insertionPoint, getStdStringFunctionCallStatement(), rewrite);
		for(IASTStatement statement : nullCheckedStatements) {
			ASTModifier.remove(statement, rewrite);
		}
	}
}