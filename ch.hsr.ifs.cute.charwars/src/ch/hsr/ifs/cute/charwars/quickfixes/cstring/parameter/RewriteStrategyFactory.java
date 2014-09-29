package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;

public class RewriteStrategyFactory {
	public static RewriteStrategy createRewriteStrategy(ICPPASTParameterDeclaration strParameter, ASTRewrite rewrite) {
		RewriteStrategy rewriteStrategy = null;
		IASTName strName = strParameter.getDeclarator().getName();
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)strParameter.getParent();
		IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition)functionDeclarator.getParent();
		IASTStatement statements[] = ((IASTCompoundStatement)functionDefinition.getBody()).getStatements();
		
		if(hasGuardClause(statements, strName)) {
			rewriteStrategy = new GuardClauseRewriteStrategy();
		}
		else if(hasResultVariable(statements, strName)) {
			rewriteStrategy = new ResultVariableRewriteStrategy();
		}
		else if(hasNullCheck(statements, strName)) {
			rewriteStrategy = new NullCheckRewriteStrategy();
		}
		else {
			rewriteStrategy = new EmptyRewriteStrategy();
		}
		
		rewriteStrategy.setRewrite(rewrite);
		rewriteStrategy.setStrParameter(strParameter);
		rewriteStrategy.setStatements(statements);
		return rewriteStrategy;
	}
	
	private static boolean hasGuardClause(IASTStatement[] bodyStatements, IASTName strName) {
		return CheckAnalyzer.findGuardClause(strName, bodyStatements) != null;
	}
	
	private static boolean hasNullCheck(IASTStatement[] bodyStatements, IASTName strName) {
		return CheckAnalyzer.findNullCheck(strName, bodyStatements) != null;
	}
	
	private static boolean hasResultVariable(IASTStatement[] bodyStatements, IASTName strName) {
		//check if null check exists
		IASTIfStatement nullCheck = CheckAnalyzer.findNullCheck(strName, bodyStatements);
		if(nullCheck == null) return false;
		
		//check if result variable exists
		IASTName resultVariableName = ASTAnalyzer.getResultVariableName(bodyStatements);
		if(resultVariableName == null) return false;
		
		//check if clause assigns to result variable
		IASTStatement[] nullCheckedStatements = CheckAnalyzer.getNullCheckedStatements(strName, bodyStatements);
		if(nullCheckedStatements.length == 0) return false;
		IASTStatement lastClauseStatement = nullCheckedStatements[nullCheckedStatements.length-1];
		
		if(!assignsToVariable(lastClauseStatement, resultVariableName)) {
			return false;
		}
		
		IASTDeclarationStatement resultVariableDeclaration = ASTAnalyzer.getVariableDeclaration(resultVariableName, bodyStatements);
		if(resultVariableDeclaration != null) {
			int resultVariableIndex = Arrays.asList(bodyStatements).indexOf(resultVariableDeclaration);
			int nullCheckIndex = Arrays.asList(bodyStatements).indexOf(nullCheck);
			if(nullCheckIndex > resultVariableIndex) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean assignsToVariable(IASTStatement statement, IASTName variableName) {
		if(statement instanceof IASTExpressionStatement) {
			IASTExpression expression = ((IASTExpressionStatement)statement).getExpression();
			if(BEAnalyzer.isAssignment(expression)) {
				IASTExpression variable = ((IASTBinaryExpression)expression).getOperand1();
				if(variable instanceof IASTIdExpression) {
					IASTName name = ((IASTIdExpression)variable).getName();
					return name.resolveBinding().equals(variableName.resolveBinding());
				}
			}
		}
		return false;
	}
}
