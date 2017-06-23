package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

public class ResultVariableRewriteStrategy extends RewriteStrategy {
	@Override
	protected IASTCompoundStatement getStdStringOverloadBody() {
		final IASTCompoundStatement body = ExtendedNodeFactory.newCompoundStatement();
		final IASTName resultVariableName = ASTAnalyzer.getResultVariableName(statements);
		final IASTDeclarationStatement resultVariableDeclaration = ASTAnalyzer.getVariableDeclaration(resultVariableName, statements);
		body.addStatement(resultVariableDeclaration.copy(CopyStyle.withLocations));

		final IASTStatement[] nullCheckedStatements = CheckAnalyzer.getNullCheckedStatements(strName, statements);
		for(final IASTStatement statement : nullCheckedStatements) {
			body.addStatement(statement.copy(CopyStyle.withLocations));
		}

		final IASTStatement returnStatement = ExtendedNodeFactory.newReturnStatement(ExtendedNodeFactory.newIdExpression(resultVariableName.toString()));
		body.addStatement(returnStatement);
		return body;
	}

	@Override
	public void adaptCStringOverload() {
		final IASTName resultVariableName = ASTAnalyzer.getResultVariableName(statements);
		final IASTStatement nullCheckClause = CheckAnalyzer.getNullCheckClause(strName, statements);
		final IASTIdExpression resultVariable = ExtendedNodeFactory.newIdExpression(resultVariableName.toString());
		final IASTExpression assignment = ExtendedNodeFactory.newAssignment(resultVariable, getStdStringFunctionCallExpression());
		final IASTExpressionStatement expressionStatement = ExtendedNodeFactory.newExpressionStatement(assignment);
		final IASTCompoundStatement compoundStatement = ExtendedNodeFactory.newCompoundStatement(expressionStatement);
		ASTModifier.replace(nullCheckClause, compoundStatement, getMainRewrite());
	}

	@Override
	protected boolean shouldCopyDefaultValueOfParameter() {
		return false;
	}
}
