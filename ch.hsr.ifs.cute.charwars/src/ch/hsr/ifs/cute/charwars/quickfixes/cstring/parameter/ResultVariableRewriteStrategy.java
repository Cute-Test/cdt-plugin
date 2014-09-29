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
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;

public class ResultVariableRewriteStrategy extends RewriteStrategy {
	@Override
	protected IASTCompoundStatement getStdStringOverloadBody() {
		IASTCompoundStatement stdStringOverloadBody = ExtendedNodeFactory.newCompoundStatement();
		IASTName resultVariableName = ASTAnalyzer.getResultVariableName(statements);
		IASTDeclarationStatement resultVariableDeclaration = ASTAnalyzer.getVariableDeclaration(resultVariableName, statements);
		stdStringOverloadBody.addStatement(resultVariableDeclaration.copy(CopyStyle.withLocations));
		
		IASTStatement[] nullCheckedStatements = CheckAnalyzer.getNullCheckedStatements(strName, statements);
		for(IASTStatement statement : nullCheckedStatements) {
			stdStringOverloadBody.addStatement(statement.copy(CopyStyle.withLocations));
		}
		
		IASTStatement returnStatement = ExtendedNodeFactory.newReturnStatement(ExtendedNodeFactory.newIdExpression(resultVariableName.toString()));
		stdStringOverloadBody.addStatement(returnStatement);
		return stdStringOverloadBody;
	}
	
	@Override
	public void adaptCStringOverload() {
		IASTName resultVariableName = ASTAnalyzer.getResultVariableName(statements);
		IASTStatement nullCheckClause = CheckAnalyzer.getNullCheckClause(strName, statements);
		IASTIdExpression resultVariable = ExtendedNodeFactory.newIdExpression(resultVariableName.toString());
		IASTExpression assignment = ExtendedNodeFactory.newAssignment(resultVariable, getStdStringFunctionCallExpression());
		IASTExpressionStatement expressionStatement = ExtendedNodeFactory.newExpressionStatement(assignment);
		IASTCompoundStatement compoundStatement = ExtendedNodeFactory.newCompoundStatement(expressionStatement);
		ASTModifier.replace(nullCheckClause, compoundStatement, rewrite);
	}
}
