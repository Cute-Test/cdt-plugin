package ch.hsr.ifs.cute.charwars.asttools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;

import ch.hsr.ifs.cute.charwars.asttools.FindIdExpressionsVisitor;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.LiteralAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.TypeAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.UEAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;

public class ASTAnalyzer {
	public static boolean isFunctionDefinitionParameterDeclaration(IASTParameterDeclaration declaration) {
		return declaration.getParent().getParent() instanceof IASTFunctionDefinition;
	}
	
	public static boolean isLValueInAssignment(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		if(BEAnalyzer.isAssignment(parent)) {
			IASTBinaryExpression assignment = (IASTBinaryExpression)parent;
			return assignment.getOperand1() == idExpression;
		}
		return false;
	}
	
	public static boolean isArraySubscriptExpression(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		if(parent instanceof IASTArraySubscriptExpression) {
			IASTArraySubscriptExpression asExpression = (IASTArraySubscriptExpression)parent;
			return asExpression.getArrayExpression() == idExpression;
		}
		return false;
	}
	
	public static boolean isArrayLengthCalculation(IASTIdExpression idExpression) {
		if(UEAnalyzer.isDereferenceExpression(idExpression.getParent()))
			return false;
		
		IASTNode currentNode = idExpression;
		while(currentNode != null) {
			currentNode = currentNode.getParent();
			if(BEAnalyzer.isDivision(currentNode))
				break;
		}
		
		if(currentNode == null)
			return false;
		
		IASTBinaryExpression division = (IASTBinaryExpression)currentNode;
		IASTNode dividend = division.getOperand1();
		IASTNode divisor = division.getOperand2();
		return UEAnalyzer.isSizeofExpression(dividend) && UEAnalyzer.isSizeofExpression(divisor);
	}
	
	//dividend / divisor - subtrahend
	public static boolean isStringLengthCalculation(IASTIdExpression idExpression) {
		if(UEAnalyzer.isDereferenceExpression(idExpression.getParent()))
			return false;
		
		IASTNode currentNode = idExpression;
		while(currentNode != null) {
			currentNode = currentNode.getParent();
			if(BEAnalyzer.isSubtraction(currentNode))
				break;
		}
		
		if(currentNode == null)
			return false;
		
		IASTNode minuend = ((IASTBinaryExpression)currentNode).getOperand1();
		IASTNode subtrahend = ((IASTBinaryExpression)currentNode).getOperand2();
		if(BEAnalyzer.isDivision(minuend) && LiteralAnalyzer.isInteger(subtrahend, 1)) {
			IASTBinaryExpression division = (IASTBinaryExpression)minuend;
			IASTNode dividend = division.getOperand1(); 
			IASTNode divisor = division.getOperand2();
			return UEAnalyzer.isSizeofExpression(dividend) && UEAnalyzer.isSizeofExpression(divisor);
		}
		
		return false;
	}
	
	public static boolean isPartOfStringCheck(IASTIdExpression node, boolean isEqualityCheck) {
		IASTNode parent = node.getParent();
		if(FunctionAnalyzer.isCallToFunction(parent, Function.STRCMP) || FunctionAnalyzer.isCallToFunction(parent, Function.WCSCMP)) {
			IASTExpression strcmpCall = (IASTExpression)parent;
			IASTNode strcmpParent = strcmpCall.getParent();
			
			if(isEqualityCheck) {
				if(UEAnalyzer.isLogicalNot(strcmpParent)) {
					return true;
				}
				else if(BEAnalyzer.isEqualityCheck(strcmpParent)) {
					IASTBinaryExpression equalityCheck = (IASTBinaryExpression)strcmpParent;
					return hasZeroOperand(equalityCheck);
				}
			}
			else {
				if(BEAnalyzer.isInequalityCheck(strcmpParent)) {
					IASTBinaryExpression inequalityCheck = (IASTBinaryExpression)strcmpParent;
					return hasZeroOperand(inequalityCheck);
				}
				else if(isCondition(strcmpCall) || isAssignedToBoolean(strcmpCall) || isAssert(strcmpCall)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean hasZeroOperand(IASTBinaryExpression binaryExpression) {
		IASTNode op1 = binaryExpression.getOperand1();
		IASTNode op2 = binaryExpression.getOperand2();
		return LiteralAnalyzer.isZero(op1) || LiteralAnalyzer.isZero(op2);
	}
	
	public static IASTStatement getStatement(IASTNode node) {
		IASTNode result = node;
		while(result != null && !(result instanceof IASTStatement)) {
			result = result.getParent();
		}
		return result == null ? null : (IASTStatement)result;
	}
	
	public static boolean isCheckedIfEqualToNull(IASTNode node) {
		IASTNode parent = node.getParent();
		if(BEAnalyzer.isEqualityCheck(parent)) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)parent;
			IASTNode otherOperand = (comparison.getOperand1() == node) ? comparison.getOperand2() : comparison.getOperand1();
			return isNullExpression(otherOperand);
		}
		else if(UEAnalyzer.isLogicalNot(parent)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isCheckedIfNotEqualToNull(IASTNode node) {
		IASTNode parent = node.getParent();
		
		if(BEAnalyzer.isInequalityCheck(parent)) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)parent;
			IASTNode otherOperand = (comparison.getOperand1() == node) ? comparison.getOperand2() : comparison.getOperand1();
			return isNullExpression(otherOperand);
		}
		return isCondition(node) || isAssignedToBoolean(node) || isAssert(node); 
	}
	
	public static boolean isCondition(IASTNode node) {
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
	
	public static boolean isAssignedToBoolean(IASTNode node) {
		IASTNode parent = node.getParent().getOriginalNode();
		
		if(BEAnalyzer.isAssignment(parent)) { 
			IASTBinaryExpression assignment = (IASTBinaryExpression)parent;
			IType expressionType = assignment.getOperand1().getExpressionType();
			return TypeAnalyzer.getBasicKind(expressionType) == Kind.eBoolean;
		}
		else if(parent instanceof IASTEqualsInitializer && parent.getParent() instanceof IASTDeclarator) { 
			IASTDeclarator declarator = (IASTDeclarator)parent.getParent();
			IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration)declarator.getParent()).getDeclSpecifier();
			if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
				IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
				return simpleDeclSpecifier.getType() == IASTSimpleDeclSpecifier.t_bool;
			}
		}
		return false;
	}
	
	public static boolean isAssert(IASTNode node) {
		String rawSignature = node.getParent().getRawSignature();
		return rawSignature.contains(Constants.ASSERT + "(") || rawSignature.contains(Constants.ASSERT.toUpperCase() + "(");
	}
	
	public static boolean isReturned(IASTNode node) {
		return node.getParent() instanceof IASTReturnStatement;
	}
	
	private static boolean isNullExpression(IASTNode node) {
		if(node instanceof IASTLiteralExpression) {
			IASTLiteralExpression literalExpression = (IASTLiteralExpression)node;
			String literalValue = String.valueOf(literalExpression.getValue());
			return Constants.NULL_VALUES.contains(literalValue);
		}
		return false;
	}

	public static boolean isConversionToCharPointer(IASTNode node, boolean isConst) {
		if(isConst) {
			return FunctionAnalyzer.isCallToMemberFunction(node, Function.C_STR);
		}
		else {
			if(UEAnalyzer.isAddressOperatorExpression(node)) {
				IASTUnaryExpression addressOperatorExpression = (IASTUnaryExpression)node;
				if(UEAnalyzer.isDereferenceExpression(addressOperatorExpression.getOperand())) {
					IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)addressOperatorExpression.getOperand();
					if(FunctionAnalyzer.isCallToMemberFunction(dereferenceExpression.getOperand(), Function.BEGIN)) {
						return true;
					}
				}
			}

		}
		return false;
	}
	
	public static IASTNode getEnclosingBlock(IASTNode node) {
		IASTNode block = node;
		while(block != null && !(block instanceof IASTCompoundStatement || 
				block instanceof ICPPASTNamespaceDefinition ||
				block instanceof IASTCompositeTypeSpecifier)) {
			block = block.getParent();
		}
		
		if(block == null)
			block = node.getTranslationUnit();
		
		return block;
	}
	
	public static boolean isNodeComparedToStrlen(IASTNode node, boolean equalityComparison) {
		IASTNode parent = node.getParent();
		if(parent instanceof IASTBinaryExpression) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)parent;
			int operator = comparison.getOperator();
			boolean comparedForEquality = (operator == IASTBinaryExpression.op_equals);
			boolean comparedForInequality;
			IASTExpression strlenOperand;
			
			if(node == comparison.getOperand1()) {
				comparedForInequality = (operator == IASTBinaryExpression.op_notequals) || (operator == IASTBinaryExpression.op_lessThan);
				strlenOperand = comparison.getOperand2();
			}
			else {
				comparedForInequality = (operator == IASTBinaryExpression.op_notequals) || (operator == IASTBinaryExpression.op_greaterThan);
				strlenOperand = comparison.getOperand1();
			}
			
			//check if operator is valid
			if(!comparedForEquality && !comparedForInequality)
				return false;
			
			if(equalityComparison) {
				return comparedForEquality && (FunctionAnalyzer.isCallToFunction(strlenOperand, Function.STRLEN) || FunctionAnalyzer.isCallToMemberFunction(strlenOperand, Function.SIZE));
			}
			else {
				return comparedForInequality && (FunctionAnalyzer.isCallToFunction(strlenOperand, Function.STRLEN) || FunctionAnalyzer.isCallToMemberFunction(strlenOperand, Function.SIZE));
			}
		}
		return false;
	}
	
	public static boolean isNameAvailable(String name, IASTNode nodeInBlock) {
		IASTNode block = getEnclosingBlock(nodeInBlock);
		IASTNode blockParent = block.getParent();
		
		if(blockParent instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDefinition = (IASTFunctionDefinition)blockParent;
			ICPPASTFunctionDeclarator funcDeclarator = (ICPPASTFunctionDeclarator)funcDefinition.getDeclarator();
			for(IASTParameterDeclaration parameterDeclaration : funcDeclarator.getParameters()) {
				if(parameterDeclaration.getDeclarator().getName().toString().equals(name)) {
					return false;
				}
			}
		}
		
		FindIdExpressionsVisitor visitor = new FindIdExpressionsVisitor(name);
		block.accept(visitor);
		return !visitor.hasFoundIdExpression();
	}
	
	public static IASTNode extractStdStringArg(IASTNode node) {
		IASTNode result = node;
		
		if(isConversionToCharPointer(node, true)) {			//str.c_str()
			IASTFunctionCallExpression c_strCall = (IASTFunctionCallExpression)node;
			IASTFieldReference fieldReference = (IASTFieldReference)c_strCall.getFunctionNameExpression();
			result = fieldReference.getFieldOwner();
		}
		else if(isConversionToCharPointer(node, false)) {	//&str.begin()
			IASTUnaryExpression addressOperatorExpression = (IASTUnaryExpression)node;
			IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)addressOperatorExpression.getOperand();
			IASTFunctionCallExpression beginCall = (IASTFunctionCallExpression)dereferenceExpression.getOperand();
			IASTFieldReference fieldReference = (IASTFieldReference)beginCall.getFunctionNameExpression();
			result = fieldReference.getFieldOwner(); 
		}

		return result;
	}

	public static boolean isStdCout(IASTNode node) {
		if(node instanceof IASTIdExpression) {
			IASTName name = ((IASTIdExpression)node.getOriginalNode()).getName();
			String nameStr = name.getRawSignature();
			return nameStr.equals(Constants.STD_COUT) || nameStr.equals(Constants.COUT);
		}
		return false;
	}
	
	public static boolean isLeftShiftExpressionToStdCout(IASTNode node) {
		if(BEAnalyzer.isLeftShiftExpression(node) && node instanceof ICPPASTBinaryExpression) {
			ICPPASTBinaryExpression leftShiftExpression = (ICPPASTBinaryExpression)node;
			IASTExpression leftOperand = leftShiftExpression.getOperand1();
			return isStdCout(leftOperand) ? true : isLeftShiftExpressionToStdCout(leftOperand);
		}
		return false;
	}
	
	public static IASTNode getMarkedNode(IASTTranslationUnit ast, int offset, int length) {
		return ast.getNodeSelector(null).findNode(offset, length);
	}

	public static boolean isCheckedForEmptiness(IASTNode node, boolean empty) {
		if(UEAnalyzer.isDereferenceExpression(node.getParent())) {
			IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)node.getParent();
			IASTNode parent = dereferenceExpression.getParent();
			if((empty && BEAnalyzer.isEqualityCheck(parent)) || (!empty && BEAnalyzer.isInequalityCheck(parent))) {
				IASTBinaryExpression check = (IASTBinaryExpression)parent;
				return hasZeroOperand(check);
			}
			else if(empty && UEAnalyzer.isLogicalNot(parent)) {
				return true;
			}
			else if(!empty && (isCondition(dereferenceExpression) || isAssignedToBoolean(dereferenceExpression) || isAssert(dereferenceExpression))) {
				return true;
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
			
			boolean isCheckedIfEqualToNull = isCheckedIfEqualToNull(idExpression);
			boolean isCheckedIfNotEqualToNull = isCheckedIfNotEqualToNull(idExpression);
			boolean checkIsCondition = check == ifCondition;
			IASTNode checkParent = check.getParent();
			boolean checkIsPartOfLogicalOrCondition = BEAnalyzer.isLogicalOr(checkParent) && checkParent == ifCondition;
			boolean checkIsPartOfLogicalAndCondition = BEAnalyzer.isLogicalAnd(checkParent) && checkParent == ifCondition;
			
			if(equalCheck) {
				return isCheckedIfEqualToNull && (checkIsCondition || checkIsPartOfLogicalOrCondition);
			}
			else {
				return isCheckedIfNotEqualToNull && (checkIsCondition || checkIsPartOfLogicalAndCondition);
			}
		}
		return false;
	}
	
	public static boolean hasThenClauseWithNonNullString(IASTStatement statement, IASTName strName) {
		if(!(statement instanceof IASTIfStatement)) {
			return false;
		}
		
		return isNullComparison(statement, strName, false);
	}
	
	public static boolean hasElseClauseWithNonNullString(IASTStatement statement, IASTName strName) {
		if(!(statement instanceof IASTIfStatement)) {
			return false;
		}
		
		boolean isNullComparison = isNullComparison(statement, strName, true);
		boolean hasElseClause = ((IASTIfStatement)statement).getElseClause() != null;
		return isNullComparison && hasElseClause;
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
	
	private static boolean isAssertStatement(IASTStatement statement, IASTName name) {
		if(statement instanceof IASTExpressionStatement) {
			IASTExpressionStatement expressionStatement = (IASTExpressionStatement)statement;
			IASTExpression expression = expressionStatement.getExpression();
			
			IASTIdExpression idExpression = findFirstIdExpression(name, expression);
			if(idExpression == null) return false;
			
			if(isCheckedIfNotEqualToNull(idExpression)) {
				IASTNode booleanExpression = getEnclosingBoolean(idExpression);
				return isAssert(booleanExpression);
			}
		}
		return false;
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
	
	public static IASTName getResultVariableName(IASTStatement[] statements) {
		if(statements.length == 0) {
			return null;
		}
		
		IASTStatement lastStatement = statements[statements.length-1];
		if(lastStatement instanceof IASTReturnStatement) {
			IASTExpression returnValue = ((IASTReturnStatement)lastStatement).getReturnValue();
			if(returnValue instanceof IASTIdExpression) {
				return ((IASTIdExpression)returnValue).getName();
			}
		}
		
		return null;
	}
	
	public static IASTDeclarationStatement getVariableDeclaration(IASTName name, IASTStatement[] statements) {
		IBinding nameBinding = name.resolveBinding();
		for(IASTStatement statement : statements) {
			if(statement instanceof IASTDeclarationStatement) {
				IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement)statement;
				IASTDeclaration declaration = declarationStatement.getDeclaration();
				if(declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
					for(IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
						if(declarator.getName().resolveBinding().equals(nameBinding)) {
							return declarationStatement;
						}
					}
				}
			}
		}
		return null;
	}
	
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
	
	public static IASTNode getEnclosingBoolean(IASTNode node) {
		while(node != null && !isCondition(node) && !isAssignedToBoolean(node) && !isAssert(node) && !isReturned(node) && !UEAnalyzer.isBracketExpression(node.getParent())) {
			node = node.getParent();
		}
		return node;
	}
	
	public static boolean isDereferencedToChar(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		
		if(UEAnalyzer.isDereferenceExpression(parent)) {
			return true;
		}
		else if(UEAnalyzer.isBracketExpression(parent) && UEAnalyzer.isDereferenceExpression(parent.getParent())) {
			return true;
		}
		else if(BEAnalyzer.isAddition(parent) && UEAnalyzer.isBracketExpression(parent.getParent()) && UEAnalyzer.isDereferenceExpression(parent.getParent().getParent())) {
			return true;
		}
		
		return false;
	}
	
	public static IASTStatement getTopLevelParentStatement(IASTNode node) {
		IASTNode current = node;
		IASTNode parent = node.getParent();
		
		while(parent != null && parent.getParent() != null && !(parent.getParent() instanceof IASTFunctionDefinition)) {
			current = current.getParent();
			parent = parent.getParent();
		}
		
		if(parent instanceof IASTCompoundStatement && current instanceof IASTStatement) {
			return (IASTStatement)current;
		}
		return null;
	}
	
	public static boolean modifiesCharPointer(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		boolean isLValue = isLValueInAssignment(idExpression) && ((IASTBinaryExpression)parent).getOperand2() instanceof IASTBinaryExpression;
		boolean isPlusAssigned = BEAnalyzer.isPlusAssignment(parent) && ((IASTBinaryExpression)parent).getOperand1() == idExpression;
		boolean isIncremented = UEAnalyzer.isIncrementation(parent);
		return isLValue || isPlusAssigned || isIncremented;
	}
}