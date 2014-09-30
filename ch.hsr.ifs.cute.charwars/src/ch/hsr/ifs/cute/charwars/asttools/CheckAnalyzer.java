package ch.hsr.ifs.cute.charwars.asttools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;

import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.LiteralAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.TypeAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.UEAnalyzer;

public class CheckAnalyzer {
	public static IASTStatement[] getNullCheckedStatements(IASTName name, IASTStatement[] statements) {
		List<IASTStatement> nullCheckedStatements = new ArrayList<IASTStatement>();
		IASTIfStatement nullCheck = findNullCheck(name, statements);
		IASTNode guardClause = findGuardClause(name, statements); 
		
		if(nullCheck != null) {
			IASTStatement nullCheckClause = getNullCheckClause(name, statements);
			if(nullCheckClause instanceof IASTCompoundStatement) {
				IASTCompoundStatement compoundClause = (IASTCompoundStatement)nullCheckClause;
				for(IASTStatement statement : compoundClause.getStatements()) {
					nullCheckedStatements.add(statement);
				}
			}
			else {
				nullCheckedStatements.add(nullCheckClause);
			}
		}
		else if(guardClause != null) {
			int guardClauseIndex = Arrays.asList(statements).indexOf(guardClause);
			for(int i = guardClauseIndex + 1; i < statements.length; ++i) {
				nullCheckedStatements.add(statements[i]);
			}
		}
		return nullCheckedStatements.toArray(new IASTStatement[0]);
	}
	
	public static IASTIfStatement findNullCheck(IASTName name, IASTStatement[] statements) {
		IASTIfStatement nullCheck = null;
		for(IASTStatement statement : statements) {
			if(hasThenClauseWithNonNullString(statement, name) || hasElseClauseWithNonNullString(statement, name)) {
				nullCheck = (IASTIfStatement)statement;
				break;
			}
		}
		return nullCheck;
	}
	
	public static IASTStatement findGuardClause(IASTName name, IASTStatement[] statements) {
		for(IASTStatement statement : statements) {
			if(isNullComparison(statement, name, true)) {
				IASTIfStatement ifStatement = (IASTIfStatement)statement;
				IASTStatement thenClause = ifStatement.getThenClause();
				IASTStatement lastStatement = thenClause;
				if(thenClause instanceof IASTCompoundStatement) {
					IASTCompoundStatement compoundThenClause = (IASTCompoundStatement)thenClause;
					lastStatement = compoundThenClause.getStatements()[compoundThenClause.getStatements().length - 1];
				}
				
				if(lastStatement instanceof IASTReturnStatement && ifStatement.getElseClause() == null) {
					return ifStatement;
				}
			}
			else if(isAssertStatement(statement, name)) {
				return statement;
			}
		}
		
		return null;
	}
	
	public static IASTStatement getNullCheckClause(IASTName name, IASTStatement[] statements) {
		IASTStatement nullCheckClause = null;
		IASTIfStatement nullCheck = findNullCheck(name, statements);
		if(nullCheck != null) {
			if(hasThenClauseWithNonNullString(nullCheck, name)) {
				nullCheckClause = nullCheck.getThenClause();
			}
			else {
				nullCheckClause = nullCheck.getElseClause();
			}
		}
		return nullCheckClause;
	}
	
	private static boolean hasThenClauseWithNonNullString(IASTStatement statement, IASTName strName) {
		if(!(statement instanceof IASTIfStatement)) {
			return false;
		}
		
		return isNullComparison(statement, strName, false);
	}
	
	private static boolean hasElseClauseWithNonNullString(IASTStatement statement, IASTName strName) {
		if(!(statement instanceof IASTIfStatement)) {
			return false;
		}
		
		boolean isNullComparison = isNullComparison(statement, strName, true);
		boolean hasElseClause = ((IASTIfStatement)statement).getElseClause() != null;
		return isNullComparison && hasElseClause;
	}
	
	private static boolean isNullComparison(IASTStatement statement, IASTName name, boolean equalCheck) {
		if(statement instanceof IASTIfStatement) {
			IASTIfStatement ifStatement = (IASTIfStatement)statement;
			IASTExpression ifCondition = ifStatement.getConditionExpression();
			
			IASTIdExpression idExpression = findFirstIdExpression(name, ifCondition);
			if(idExpression == null) return false;
			
			IASTNode check = idExpression.getParent();
			if((check == ifStatement || BEAnalyzer.isLogicalAnd(check)) && !equalCheck) {
				return true;
			}
			
			boolean isNodeComparedToNull = isNodeComparedToNull(idExpression, equalCheck);
			IASTNode checkParent = check.getParent();
			boolean checkIsCondition = check == ifCondition;
			boolean checkParentIsCondition = checkParent == ifCondition;
			boolean checkIsPartOfLogicalOrCondition = BEAnalyzer.isLogicalOr(checkParent) && checkParentIsCondition;
			boolean checkIsPartOfLogicalAndCondition = BEAnalyzer.isLogicalAnd(checkParent) && checkParentIsCondition;
			
			if(equalCheck) {
				return isNodeComparedToNull && (checkIsCondition || checkIsPartOfLogicalOrCondition);
			}
			else {
				return isNodeComparedToNull && (checkIsCondition || checkIsPartOfLogicalAndCondition);
			}
		}
		return false;
	}
	
	private static boolean isAssertStatement(IASTStatement statement, IASTName name) {
		if(statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement expressionStatement = (IASTExpressionStatement)statement;
			IASTExpression expression = expressionStatement.getExpression();
			
			IASTIdExpression idExpression = findFirstIdExpression(name, expression);
			if(idExpression == null) return false;
			
			if(isNodeComparedToNull(idExpression, false)) {
				IASTNode booleanExpression = getEnclosingBoolean(idExpression);
				return isAssert(booleanExpression);
			}
		}
		return false;
	}
	
	private static IASTIdExpression findFirstIdExpression(IASTName name, IASTNode node) {
		if(node instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)node;
			if(idExpression.getName().resolveBinding().equals(name.resolveBinding())) {
				return idExpression;
			}
		}
		
		for(IASTNode child : node.getChildren()) {
			IASTIdExpression result = findFirstIdExpression(name, child);
			if(result != null) {
				return result;
			}
		}
		return null;
	}
	
	public static boolean isCheckedForEmptiness(IASTNode node, boolean empty) {
		IASTNode parent = node.getParent();
		if(UEAnalyzer.isDereferenceExpression(parent)) {
			return isNodeComparedToZero(parent, empty);
		}
		return false;
	}
	
	public static boolean isPartOfStringCheck(IASTIdExpression node, boolean equalityComparison) {
		IASTNode parent = node.getParent();
		if(FunctionAnalyzer.isCallToFunction(parent, Function.STRCMP) || FunctionAnalyzer.isCallToFunction(parent, Function.WCSCMP)) {		
			return isNodeComparedToZero(parent, equalityComparison);
		}
		return false;
	}
	
	public static boolean isNodeComparedToNull(IASTNode node, boolean equalityComparison) {
		return isNodeComparedTo(node, equalityComparison, false);
	}
	
	public static boolean isNodeComparedToZero(IASTNode node, boolean equalityComparison) {
		return isNodeComparedTo(node, equalityComparison, true);
	}
	
	private static boolean isNodeComparedTo(IASTNode node, boolean equalityComparison, boolean zeroOnly) {
		IASTNode parent = node.getParent();
		
		if(BEAnalyzer.isComparison(parent, equalityComparison)) {
			IASTNode otherOperand = BEAnalyzer.getOtherOperand(node);
			
			if(zeroOnly) {
				return LiteralAnalyzer.isZero(otherOperand);
			}
			else {
				return LiteralAnalyzer.isNullExpression(otherOperand);
			}
		}
		
		if(equalityComparison) {
			return UEAnalyzer.isLogicalNot(parent);
		}
		else {
			return isCondition(node) || isAssignedToBoolean(node) || isAssert(node);
		}
	}
	
	public static boolean isNodeComparedToStrlen(IASTNode node, boolean equalityComparison) {
		IASTNode parent = node.getParent();
		if(parent instanceof IASTBinaryExpression) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)parent;
			int operator = comparison.getOperator();
			boolean equals = (operator == IASTBinaryExpression.op_equals);
			boolean not_equals = (operator == IASTBinaryExpression.op_notequals);
			
			
			IASTExpression operand = BEAnalyzer.getOtherOperand(node);
			
			if(BEAnalyzer.isOp1(node)) {
				not_equals = not_equals || (operator == IASTBinaryExpression.op_lessThan);
			}
			else {
				not_equals = not_equals || (operator == IASTBinaryExpression.op_greaterThan);
			}
			
			//check if operator is valid
			if(!equals && !not_equals)
				return false;
			
			boolean isStrlenOperand = FunctionAnalyzer.isCallToFunction(operand, Function.STRLEN) || FunctionAnalyzer.isCallToMemberFunction(operand, Function.SIZE);
			if(equalityComparison) {
				return equals && isStrlenOperand;
			}
			else {
				return not_equals && isStrlenOperand;
			}
		}
		return false;
	}
	
	public static IASTNode getEnclosingBoolean(IASTNode node) {
		while(node != null && !isCondition(node) && !isAssignedToBoolean(node) && !isAssert(node) && !isReturned(node) && !UEAnalyzer.isBracketExpression(node.getParent())) {
			node = node.getParent();
		}
		return node;
	}
	
	private static boolean isCondition(IASTNode node) {
		IASTNode parent = node.getParent();
		
		if(parent instanceof IASTIfStatement) {
			IASTIfStatement ifStatement = (IASTIfStatement)parent;
			return ifStatement.getConditionExpression() == node;
		}
		else if(parent instanceof IASTWhileStatement) {
			IASTWhileStatement whileStatement = (IASTWhileStatement)parent;
			return whileStatement.getCondition() == node;
		}
		else if(parent instanceof IASTForStatement) {
			IASTForStatement forStatement = (IASTForStatement)parent;
			return forStatement.getConditionExpression() == node;
		}
		else if(parent instanceof IASTConditionalExpression) {
			IASTConditionalExpression conditionalExpression = (IASTConditionalExpression)parent;
			return conditionalExpression.getLogicalConditionExpression() == node;
		}
		else if(parent instanceof IASTDoStatement) {
			IASTDoStatement doStatement = (IASTDoStatement)parent;
			return doStatement.getCondition() == node;
		}
		
		return BEAnalyzer.isLogicalAnd(parent) || BEAnalyzer.isLogicalOr(parent);
	}
	
	private static boolean isAssignedToBoolean(IASTNode node) {
		IASTNode parent = node.getParent().getOriginalNode();
		
		if(BEAnalyzer.isAssignment(parent)) {
			IType expressionType = BEAnalyzer.getOperand1(parent).getExpressionType();
			return TypeAnalyzer.getBasicKind(expressionType) == Kind.eBoolean;
		}
		else if(parent instanceof IASTEqualsInitializer && parent.getParent() instanceof IASTDeclarator) {
			IASTDeclarator declarator = (IASTDeclarator)parent.getParent();
			return DeclaratorAnalyzer.hasBoolType(declarator);
		}
		return false;
	}
	
	private static boolean isAssert(IASTNode node) {
		String rawSignature = node.getParent().getRawSignature();
		return rawSignature.contains(Constants.ASSERT + "(") || rawSignature.contains(Constants.ASSERT.toUpperCase() + "(");
	}
	
	private static boolean isReturned(IASTNode node) {
		return node.getParent() instanceof IASTReturnStatement;
	}
}
