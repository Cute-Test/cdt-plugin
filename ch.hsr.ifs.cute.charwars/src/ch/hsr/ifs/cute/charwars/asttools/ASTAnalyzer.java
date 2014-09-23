package ch.hsr.ifs.cute.charwars.asttools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
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
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.charwars.asttools.FindIdExpressionsVisitor;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.constants.Function;

public class ASTAnalyzer {
	public static boolean isCString(IASTDeclarator declarator) {
		boolean hasCStringType = hasCStringType(declarator, true) || hasCStringType(declarator, false); 
		return hasCStringType && (hasStringLiteralAssignment(declarator) || hasStrdupAssignment(declarator));
	}
	
	public static boolean isCStringAlias(IASTDeclarator declarator) {
		boolean hasCStringType = hasCStringType(declarator, true) || hasCStringType(declarator, false); 
		return hasCStringType && hasCStringAssignment(declarator);
	}
	
	public static boolean hasCStringAssignment(IASTDeclarator declarator) {
		IASTInitializer initializer = declarator.getInitializer();
		if(initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			IASTInitializerClause initializerClause = equalsInitializer.getInitializerClause();
			boolean isConversionToCharPointer = isConversionToCharPointer(initializerClause, true);
			if(isConversionToCharPointer) {
				IASTFunctionCallExpression cstrCall = (IASTFunctionCallExpression)initializerClause;
				IASTFieldReference fieldReference = (IASTFieldReference)cstrCall.getFunctionNameExpression();
				IASTExpression fieldOwner = fieldReference.getFieldOwner();
				return TypeAnalyzer.isStdStringType(((IASTExpression)fieldOwner).getExpressionType());
			}
		}
		return false;
	}
	
	private static boolean hasCStringType(IASTDeclarator declarator, boolean isConst) {
		IASTNode parent = declarator.getParent();
		IASTDeclSpecifier declSpecifier = null;
		
		if(parent instanceof IASTSimpleDeclaration) {
			declSpecifier = ((IASTSimpleDeclaration)parent).getDeclSpecifier(); 
		}
		else if(parent instanceof ICPPASTParameterDeclaration) {
			declSpecifier = ((ICPPASTParameterDeclaration)parent).getDeclSpecifier();
		}
		
		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
			int type = simpleDeclSpecifier.getType();
			boolean isValidType = type == IASTSimpleDeclSpecifier.t_char || 
								  type == IASTSimpleDeclSpecifier.t_wchar_t ||
								  type == IASTSimpleDeclSpecifier.t_char16_t ||
								  type == IASTSimpleDeclSpecifier.t_char32_t;
			return isValidType && isArrayXorPointer(declarator) && isConst == simpleDeclSpecifier.isConst();
		}
		return false;
	}
	
	public static boolean hasVoidType(IASTDeclSpecifier declSpecifier) {
		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
			int type = simpleDeclSpecifier.getType();
			return type == IASTSimpleDeclSpecifier.t_void;
		}
		return false;
	}
	
	public static boolean isArray(IASTDeclarator declarator) {
		return declarator instanceof IASTArrayDeclarator;
	}
	
	private static boolean isArrayXorPointer(IASTDeclarator declarator) {
		return isArray(declarator) ^ isPointer(declarator);
	}
	
	public static boolean isPointer(IASTDeclarator declarator) {
		int numberOfPointers = 0;
		for(IASTPointerOperator po : declarator.getPointerOperators()) {
			if(po instanceof IASTPointer) {
				numberOfPointers++;
			}
		}
		return numberOfPointers == 1;
	}
	
	private static boolean hasStringLiteralAssignment(IASTDeclarator declarator) {
		IASTInitializerClause initializerClause = getInitializerClause(declarator);
		if(initializerClause != null) {
			return isStringLiteral(initializerClause);
		}
		return false;
	}
	
	public static boolean hasStrdupAssignment(IASTDeclarator declarator) {
		IASTInitializerClause initializerClause = getInitializerClause(declarator);
		if(initializerClause != null) {
			return isCallToFunction(initializerClause, Function.STRDUP);
		}
		return false;
	}
	
	public static IASTInitializerClause getInitializerClause(IASTDeclarator declarator) {
		IASTInitializer initializer = declarator.getInitializer();
		if(initializer != null && initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			return equalsInitializer.getInitializerClause();
		}
		return null;
	}
	
	public static boolean isStringLiteral(IASTNode node) {
		return getLiteralKind(node) == IASTLiteralExpression.lk_string_literal;
	}
	
	public static boolean isIntegerLiteral(IASTNode node, int value) {
		return getLiteralKind(node) == IASTLiteralExpression.lk_integer_constant 
			&& getLiteralValue(node).equals(String.valueOf(value));
	}
	
	public static boolean isIntegerLiteral(IASTNode node) {
		return getLiteralKind(node) == IASTLiteralExpression.lk_integer_constant;
	}
	
	private static int getLiteralKind(IASTNode node) {
		if(node instanceof IASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression)node;
			return literal.getKind();
		}
		return -1;
	}
	
	private static String getLiteralValue(IASTNode node) {
		if(node instanceof IASTLiteralExpression) {
			IASTLiteralExpression literal = (IASTLiteralExpression)node;
			return String.valueOf(literal.getValue());
		}
		return null;
	}
	
	public static boolean isFunctionDefinitionParameterDeclaration(IASTParameterDeclaration declaration) {
		return declaration.getParent().getParent() instanceof IASTFunctionDefinition;
	}
	
	public static boolean isDereferenceExpression(IASTNode node) {
		return isUnaryExpression(node, IASTUnaryExpression.op_star);
	}
	
	public static boolean isAddressOperatorExpression(IASTNode node) {
		return isUnaryExpression(node, IASTUnaryExpression.op_amper);
	}
	
	public static boolean isSizeofExpression(IASTNode node) {
		return isUnaryExpression(node, IASTUnaryExpression.op_sizeof);
	}
	
	public static boolean isLogicalNotExpression(IASTNode node) {
		return isUnaryExpression(node, IASTUnaryExpression.op_not); 
	}
	
	public static boolean isBracketExpression(IASTNode node) {
		return isUnaryExpression(node, IASTUnaryExpression.op_bracketedPrimary);
	}
	
	public static boolean isUnaryExpression(IASTNode node, int operator) {
		if(node instanceof IASTUnaryExpression) {
			IASTUnaryExpression unaryExpression = (IASTUnaryExpression)node;
			return unaryExpression.getOperator() == operator;
		}
		return false;
	}
	
	public static boolean isSubtractionExpression(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_minus);
	}
	
	public static boolean isAdditionExpression(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_plus);
	}
	
	public static boolean isDivisionExpression(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_divide);
	}
	
	public static boolean isLeftShiftExpression(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_shiftLeft);
	}
	
	public static boolean isEqualityCheck(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_equals);
	}
	
	public static boolean isInequalityCheck(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_notequals);
	}
	
	public static boolean isAssignment(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_assign);
	}
	
	public static boolean isPlusAssignment(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_plusAssign);
	}
	
	public static boolean isLogicalAndExpression(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_logicalAnd);
	}
	
	public static boolean isLogicalOrExpression(IASTNode node) {
		return isBinaryExpression(node, IASTBinaryExpression.op_logicalOr);
	}
	
	public static boolean isBinaryExpression(IASTNode node, int operator) {
		if(node instanceof IASTBinaryExpression) {
			IASTBinaryExpression binaryExpression = (IASTBinaryExpression)node;
			return binaryExpression.getOperator() == operator;
		}
		return false;
	}
	
	public static boolean isCallToFunction(IASTNode node, Function function) {
		return isCallTo(node, function, false);
	}
	
	public static boolean isCallToMemberFunction(IASTNode node, Function memberFunction) {
		return isCallTo(node, memberFunction, true);
	}
	
	private static boolean isCallTo(IASTNode node, Function function, boolean isMemberFunction) {
		if(node instanceof IASTFunctionCallExpression) {
			IASTFunctionCallExpression functionCallExpression = (IASTFunctionCallExpression)node;
			IASTExpression functionNameExpression = functionCallExpression.getFunctionNameExpression();
			String expectedName = function.getName();
			String expectedQualifiedName = function.getQualifiedName();
			String actualName = null;
			
			if(!isMemberFunction && functionNameExpression instanceof IASTIdExpression) {
				IASTIdExpression idExpression = (IASTIdExpression)functionNameExpression;
				actualName = idExpression.getName().toString();
			}
			else if(isMemberFunction && functionNameExpression instanceof IASTFieldReference) {
				IASTFieldReference fieldReference = (IASTFieldReference)functionNameExpression;
				actualName = fieldReference.getFieldName().toString();
			}
			return expectedName.equals(actualName) || expectedQualifiedName.equals(actualName);
		}
		return false;
	}
	
	public static boolean isLValueInAssignment(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		if(isAssignment(parent)) {
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
		if(isDereferenceExpression(idExpression.getParent()))
			return false;
		
		IASTNode currentNode = idExpression;
		while(currentNode != null) {
			currentNode = currentNode.getParent();
			if(isDivisionExpression(currentNode))
				break;
		}
		
		if(currentNode == null)
			return false;
		
		IASTNode dividend = ((IASTBinaryExpression)currentNode).getOperand1();
		IASTNode divisor = ((IASTBinaryExpression)currentNode).getOperand2();
		return isSizeofExpression(dividend) && isSizeofExpression(divisor);
	}
	
	//dividend / divisor - subtrahend
	public static boolean isStringLengthCalculation(IASTIdExpression idExpression) {
		if(isDereferenceExpression(idExpression.getParent()))
			return false;
		
		IASTNode currentNode = idExpression;
		while(currentNode != null) {
			currentNode = currentNode.getParent();
			if(isSubtractionExpression(currentNode))
				break;
		}
		
		if(currentNode == null)
			return false;
		
		IASTNode minuend = ((IASTBinaryExpression)currentNode).getOperand1();
		IASTNode subtrahend = ((IASTBinaryExpression)currentNode).getOperand2();
		if(isDivisionExpression(minuend) && isIntegerLiteral(subtrahend, 1)) {
			IASTBinaryExpression division = (IASTBinaryExpression)minuend;
			IASTNode dividend = division.getOperand1(); 
			IASTNode divisor = division.getOperand2();
			
			if(isSizeofExpression(dividend) && isSizeofExpression(divisor)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isPartOfFunctionCallArg(IASTNode node, int argIndex, Function function) {
		IASTNode lastNode = node;
		IASTNode currentNode = lastNode.getParent();
		while(!isCallToFunction(currentNode, function) && (isSubtractionExpression(currentNode) || isAdditionExpression(currentNode))) {
			lastNode = currentNode;
			currentNode = lastNode.getParent();
		}
		
		if(isCallToFunction(currentNode, function)) {
			IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)currentNode;
			return functionCall.getArguments()[argIndex] == lastNode;
		}
		
		return false;
	}
	
	public static boolean isFunctionCallArg(IASTNode arg, int argIndex, Function function) {
		IASTNode parent = arg.getParent();
		if(isCallToFunction(parent, function)) {
			IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)parent;
			return functionCall.getArguments()[argIndex] == arg;
		}
		return false;
	}
	
	public static boolean isPartOfStringCheck(IASTIdExpression node, boolean isEqualityCheck) {
		IASTNode parent = node.getParent();
		if(isCallToFunction(parent, Function.STRCMP) || isCallToFunction(parent, Function.WCSCMP)) {
			IASTExpression strcmpCall = (IASTExpression)parent;
			IASTNode strcmpParent = strcmpCall.getParent();
			
			if(isEqualityCheck) {
				if(isLogicalNotExpression(strcmpParent)) {
					return true;
				}
				else if(isEqualityCheck(strcmpParent)) {
					IASTBinaryExpression equalityCheck = (IASTBinaryExpression)strcmpParent;
					return isIntegerLiteral(equalityCheck.getOperand1(), 0) || isIntegerLiteral(equalityCheck.getOperand2(), 0);
				}
			}
			else {
				if(isInequalityCheck(strcmpParent)) {
					IASTBinaryExpression inequalityCheck = (IASTBinaryExpression)strcmpParent;
					return isIntegerLiteral(inequalityCheck.getOperand1(), 0) || isIntegerLiteral(inequalityCheck.getOperand2(), 0);
				}
				else if(isCondition(strcmpCall) || isAssignedToBoolean(strcmpCall) || isAssert(strcmpCall)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static IASTStatement getStatement(IASTNode node) {
		IASTNode result = node;
		while(result != null && !(result instanceof IASTStatement)) {
			result = result.getParent();
		}
		return result == null ? null : (IASTStatement)result;
	}
	
	public static IASTFunctionCallExpression getEnclosingFunctionCall(IASTNode startNode, Function function) {
		IASTNode currentNode = startNode;
		while(!isCallToFunction(currentNode, function)) {
			currentNode = currentNode.getParent();
		}
		return (IASTFunctionCallExpression)currentNode;
	}
	
	public static boolean isCheckedIfEqualToNull(IASTNode node) {
		IASTNode parent = node.getParent();
		if(isEqualityCheck(parent)) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)parent;
			IASTNode otherOperand = (comparison.getOperand1() == node) ? comparison.getOperand2() : comparison.getOperand1();
			return isNullExpression(otherOperand);
		}
		else if(isLogicalNotExpression(parent)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isCheckedIfNotEqualToNull(IASTNode node) {
		IASTNode parent = node.getParent();
		
		if(isInequalityCheck(parent)) {
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
		
		return isLogicalAndExpression(parent) || isLogicalOrExpression(parent);
	}
	
	public static boolean isAssignedToBoolean(IASTNode node) {
		IASTNode parent = node.getParent().getOriginalNode();
		
		if(isAssignment(parent)) { 
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

	public static boolean isConstCStringParameter(IASTDeclarator declarator) {
		return hasCStringType(declarator, true);
	}
	
	public static boolean isCStringParameter(IASTDeclarator declarator) {
		return hasCStringType(declarator, false);
	}

	public static boolean isConversionToCharPointer(IASTNode node, boolean isConst) {
		if(isConst) {
			return isCallToMemberFunction(node, Function.C_STR);
		}
		else {
			if(isAddressOperatorExpression(node)) {
				IASTUnaryExpression addressOperatorExpression = (IASTUnaryExpression)node;
				if(isDereferenceExpression(addressOperatorExpression.getOperand())) {
					IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)addressOperatorExpression.getOperand();
					if(isCallToMemberFunction(dereferenceExpression.getOperand(), Function.BEGIN)) {
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
				return comparedForEquality && (isCallToFunction(strlenOperand, Function.STRLEN) || isCallToMemberFunction(strlenOperand, Function.SIZE));
			}
			else {
				return comparedForInequality && (isCallToFunction(strlenOperand, Function.STRLEN) || isCallToMemberFunction(strlenOperand, Function.SIZE));
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
		if(isLeftShiftExpression(node) && node instanceof ICPPASTBinaryExpression) {
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
		if(isDereferenceExpression(node.getParent())) {
			IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)node.getParent();
			IASTNode parent = dereferenceExpression.getParent();
			if((empty && isEqualityCheck(parent)) || (!empty && isInequalityCheck(parent))) {
				IASTBinaryExpression check = (IASTBinaryExpression)parent;
				return isIntegerLiteral(check.getOperand1(), 0) || isIntegerLiteral(check.getOperand2(), 0);
			}
			else if(empty && isLogicalNotExpression(parent)) {
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
			if((check == ifStatement || isLogicalAndExpression(check)) && !equalCheck) {
				return true;
			}
			
			boolean isCheckedIfEqualToNull = isCheckedIfEqualToNull(idExpression);
			boolean isCheckedIfNotEqualToNull = isCheckedIfNotEqualToNull(idExpression);
			boolean checkIsCondition = check == ifCondition;
			boolean checkIsPartOfLogicalOrCondition = isLogicalOrExpression(check.getParent()) && check.getParent() == ifCondition;
			boolean checkIsPartOfLogicalAndCondition = isLogicalAndExpression(check.getParent()) && check.getParent() == ifCondition;
			
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
		while(node != null && !isCondition(node) && !isAssignedToBoolean(node) && !isAssert(node) && !isReturned(node) && !isBracketExpression(node.getParent())) {
			node = node.getParent();
		}
		return node;
	}
	
	public static IASTName getFunctionName(IASTNode node) {
		IASTName functionName = null;
		if(node instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression functionCall = (ICPPASTFunctionCallExpression)node;
			IASTExpression functionNameExpression = functionCall.getFunctionNameExpression();
			if(functionNameExpression instanceof IASTIdExpression) {
				functionName = ((IASTIdExpression)functionNameExpression).getName();
			}
			else if(functionNameExpression instanceof IASTFieldReference) {
				functionName = ((IASTFieldReference)functionNameExpression).getFieldName();
			}
			
			if(functionName != null) {
				IBinding functionNameBinding = functionName.resolveBinding();
				if(functionNameBinding instanceof ICPPClassType) {
					functionName = getConstructorName(functionCall);
				}
			}
		}
		else if(node instanceof ICPPASTConstructorInitializer) {
			IASTImplicitNameOwner declarator = (IASTImplicitNameOwner)node.getParent();
			functionName = getConstructorName(declarator);
		}
		else if(node instanceof ICPPASTInitializerList || node instanceof IASTEqualsInitializer) {
			IASTNode parent = node.getParent();
			
			if(parent instanceof IASTEqualsInitializer) {
				parent = parent.getParent();
			}
			
			if((parent instanceof ICPPASTDeclarator || parent instanceof ICPPASTNewExpression) && parent instanceof IASTImplicitNameOwner) {
				IASTImplicitNameOwner declarator = (IASTImplicitNameOwner)parent;
				functionName = getConstructorName(declarator);
			}
		}
		return functionName;
	}
	
	private static IASTImplicitName getConstructorName(IASTImplicitNameOwner owner) {
		for(IASTImplicitName implicitName : owner.getImplicitNames()) {
			if(implicitName.resolveBinding() instanceof ICPPConstructor) {
				return implicitName;
			}
		}
		return null;
	}
	
	public static ICPPFunction getFunctionBinding(IASTNode node) {
		IASTName functionName = getFunctionName(node);		
		if(functionName != null) {
			IBinding binding = functionName.resolveBinding();
			if(binding instanceof ICPPFunction) {
				return (ICPPFunction) binding;
			}
		}
		return null;
	}
	
	public static int getArgIndex(IASTNode node, IASTNode idExpression) {
		IASTInitializerClause[] arguments = null;
		
		if(node instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression functionCall = (ICPPASTFunctionCallExpression)node;
			arguments = functionCall.getArguments();
		}
		else if(node instanceof ICPPASTConstructorInitializer) {
			ICPPASTConstructorInitializer constructorInitializer = (ICPPASTConstructorInitializer)node;
			arguments = constructorInitializer.getArguments();
		}
		else if(node instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList initializerList = (ICPPASTInitializerList)node;
			arguments = initializerList.getClauses();
		}
		
		if(arguments != null) {
			return Arrays.asList(arguments).indexOf(idExpression);
		}
		else if(node instanceof IASTEqualsInitializer) {
			return 0;
		}

		return -1;
	}
	
	public static IType getParameterType(IASTNode idExpression) {
		idExpression = idExpression.getOriginalNode();
		IASTNode parent = idExpression.getParent();
		int argIndex = getArgIndex(parent, idExpression);
		ICPPFunction functionBinding = getFunctionBinding(parent);
		
		if(functionBinding != null && argIndex != -1) {
			ICPPParameter parameter = functionBinding.getParameters()[argIndex];
			IType parameterType = parameter.getType();
			return parameterType;
		}
		return null;
	}
	
	public static String getStringReplacementType(IASTSimpleDeclSpecifier simpleDeclSpecifier) {
		switch(simpleDeclSpecifier.getType()) {
			case IASTSimpleDeclSpecifier.t_wchar_t:
				return StdString.STD_WSTRING;
			case IASTSimpleDeclSpecifier.t_char16_t:
				return StdString.STD_U16STRING;
			case IASTSimpleDeclSpecifier.t_char32_t:
				return StdString.STD_U32STRING;
			default:
				return StdString.STD_STRING;
		}
	}
	
	public static boolean isDereferencedToChar(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		
		if(isDereferenceExpression(parent)) {
			return true;
		}
		else if(isBracketExpression(parent) && isDereferenceExpression(parent.getParent())) {
			return true;
		}
		else if(isAdditionExpression(parent) && isBracketExpression(parent.getParent()) && isDereferenceExpression(parent.getParent().getParent())) {
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
		boolean isPlusAssigned = isPlusAssignment(parent) && ((IASTBinaryExpression)parent).getOperand1() == idExpression;
		boolean isIncremented = isUnaryExpression(parent, IASTUnaryExpression.op_prefixIncr) || isUnaryExpression(parent, IASTUnaryExpression.op_postFixIncr);
		return isLValue || isPlusAssigned || isIncremented;
	}
	
	public static IASTNode getOffset(IASTIdExpression idExpression, Context context) {
		IASTNode offset = ASTModifier.transformToPointerOffset(idExpression);
		
		if(offset == null) {
			if(context.isOffset(idExpression)) {
				offset = context.createOffsetVarIdExpression();
			}
			else {
				offset = ExtendedNodeFactory.newIntegerLiteral(0);
			}
		}
		else if(context.isOffset(idExpression)) {
			offset = ExtendedNodeFactory.newPlusExpression(context.createOffsetVarIdExpression(), (IASTExpression)offset);
		}
		
		return offset;
	}
	
	public static boolean isOffset(IASTIdExpression idExpression, Context context) {
		return context.isOffset(idExpression);
	}
	
	public static boolean hasOffset(IASTIdExpression idExpression, Function function) {
		if(isPartOfFunctionCallArg(idExpression, 0, function)) {
			return getEnclosingFunctionCall(idExpression, function) != idExpression.getParent();
		}
		return false;
	}
}