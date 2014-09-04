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
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
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
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

import ch.hsr.ifs.cute.charwars.asttools.FindIdExpressionsVisitor;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.StdString;

public class ASTAnalyzer {
	public static boolean isCString(IASTDeclarator declarator) {
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)declarator.getParent();
		IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
		return hasCStringType(declSpecifier) && isArrayOrPointer(declarator) && (hasStringLiteralAssignment(declarator) || hasStrdupAssignment(declarator));
	}
	
	public static boolean hasCStringType(IASTDeclSpecifier declSpecifier) {
		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
			int type = simpleDeclSpecifier.getType();
			return type == IASTSimpleDeclSpecifier.t_char || 
				   type == IASTSimpleDeclSpecifier.t_wchar_t ||
				   type == IASTSimpleDeclSpecifier.t_char16_t ||
				   type == IASTSimpleDeclSpecifier.t_char32_t;
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
	
	public static boolean hasStdStringType(IASTParameterDeclaration declaration) {
		IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
		IASTDeclarator declarator = declaration.getDeclarator();
		if(declSpecifier instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier)declSpecifier;
			String typeName = namedTypeSpecifier.getName().toString();
			boolean isStringType = typeName.equals(StdString.STRING) || typeName.equals(StdString.STD_STRING);
			return isStringType && declarator.getPointerOperators().length == 0;
		}
		return false;
	}
	
	public static boolean hasStdStringConstReferenceType(IASTParameterDeclaration declaration) {
		IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
		IASTDeclarator declarator = declaration.getDeclarator();
		if(declSpecifier instanceof IASTNamedTypeSpecifier) {
			IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();
			if(pointerOperators.length == 1) {
				IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier)declSpecifier;
				String typeName = namedTypeSpecifier.getName().toString();
				boolean isConstStringType = (typeName.equals(StdString.STRING) || typeName.equals(StdString.STD_STRING)) && namedTypeSpecifier.isConst();
				boolean isReference = pointerOperators[0] instanceof ICPPASTReferenceOperator;
				return isConstStringType && isReference;
			}
		}
		return false;
	}
	
	public static boolean isArray(IASTDeclarator declarator) {
		return declarator instanceof IASTArrayDeclarator && !isCString(declarator);
	}
	
	private static boolean isArrayOrPointer(IASTDeclarator declarator) {
		return declarator instanceof IASTArrayDeclarator || declarator.getPointerOperators().length > 0; 
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
			return isCallToFunction(initializerClause, Constants.STRDUP);
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
	
	public static boolean isPointer(IASTDeclarator declarator) {
		for(IASTPointerOperator po : declarator.getPointerOperators()) {
			if(po instanceof IASTPointer) {
				return true;
			}
		}
		return false;
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
	
	public static boolean isCallToFunction(IASTNode node, String fName) {
		if(node instanceof IASTFunctionCallExpression) {
			if(!(node.getChildren()[0] instanceof IASTFieldReference)) {
				IASTName functionName = (IASTName)node.getChildren()[0].getChildren()[0];
				return functionName.toString().equals(fName);	
			}
		}
		return false;
	}
	
	public static boolean isCallToMemberFunction(IASTNode node, String memberFunction) {
		if(node instanceof IASTFunctionCallExpression) {
			if(node.getChildren()[0] instanceof IASTFieldReference) {
				IASTFieldReference fieldReference = (IASTFieldReference)node.getChildren()[0];
				String fieldName = fieldReference.getFieldName().toString();
				return fieldName.equals(memberFunction);
			}
		}
		return false;
	}
	
	public static boolean isLValueInAssignment(IASTIdExpression idExpression) {
		if(isAssignment(idExpression.getParent())) {
			IASTBinaryExpression assignment = (IASTBinaryExpression)idExpression.getParent();
			return assignment.getOperand1() == idExpression;
		}
		return false;
	}
	
	public static boolean isArraySubscriptExpression(IASTIdExpression idExpression) {
		if(idExpression.getParent() instanceof IASTArraySubscriptExpression) {
			IASTArraySubscriptExpression asExpression = (IASTArraySubscriptExpression)idExpression.getParent();
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
	
	public static boolean isPartOfFunctionCallArgument(IASTNode node, int argIndex, String functionName) {
		IASTNode lastNode = node;
		IASTNode currentNode = lastNode.getParent();
		while(!isCallToFunction(currentNode, functionName) && (isSubtractionExpression(currentNode) || isAdditionExpression(currentNode))) {
			lastNode = currentNode;
			currentNode = lastNode.getParent();
		}
		
		if(isCallToFunction(currentNode, functionName)) {
			IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)currentNode;
			return functionCall.getArguments()[argIndex] == lastNode;
		}
		
		return false;
	}
	
	public static boolean isFunctionCallArgument(IASTNode arg, int argIndex, String functionName) {
		if(isCallToFunction(arg.getParent(), functionName)) {
			IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)arg.getParent();
			return functionCall.getArguments()[argIndex] == arg;
		}
		return false;
	}
	
	public static boolean isPartOfStringEqualityCheck(IASTIdExpression node) {
		IASTNode parent = node.getParent();
		if(isCallToFunction(parent, CString.STRCMP)) {
			IASTExpression strcmpCall = (IASTExpression)parent;
			if(isLogicalNotExpression(strcmpCall.getParent())) {
				return true;
			}
			else if(isEqualityCheck(strcmpCall.getParent())) {
				IASTBinaryExpression equalityCheck = (IASTBinaryExpression)strcmpCall.getParent();
				return isIntegerLiteral(equalityCheck.getOperand1(), 0) || isIntegerLiteral(equalityCheck.getOperand2(), 0);
			}
		}
		return false;
	}
	
	public static boolean isPartOfStringInequalityCheck(IASTIdExpression node) {
		if(isCallToFunction(node.getParent(), CString.STRCMP)) {
			IASTExpression strcmpCall = (IASTExpression)node.getParent();
			if(isInequalityCheck(strcmpCall.getParent())) {
				IASTBinaryExpression inequalityCheck = (IASTBinaryExpression)strcmpCall.getParent();
				return isIntegerLiteral(inequalityCheck.getOperand1(), 0) || isIntegerLiteral(inequalityCheck.getOperand2(), 0);
			}
			else if(isCondition(strcmpCall) || isAssignedToBoolean(strcmpCall) || isAssert(strcmpCall)) {
				return true;
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
	
	public static IASTFunctionCallExpression getEnclosingFunctionCall(IASTNode startNode, String functionName) {
		IASTNode currentNode = startNode;
		while(!isCallToFunction(currentNode, functionName)) {
			currentNode = currentNode.getParent();
		}
		return (IASTFunctionCallExpression)currentNode;
	}
	
	public static boolean isIfStatementCondition(IASTNode node) {
		return node.getParent() instanceof IASTIfStatement;
	}
	
	public static boolean isNegatedIfStatementCondition(IASTNode node) {
		IASTNode parent = node.getParent();
		return isLogicalNotExpression(parent) && isIfStatementCondition(parent);
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
			if(assignment.getOperand1().getExpressionType() instanceof IBasicType) {
				IASTExpression operand1 = (IASTExpression)assignment.getOperand1();
				IBasicType basicType = (IBasicType)operand1.getExpressionType();
				return basicType.getKind() == Kind.eBoolean;
			}
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
	
	public static boolean isNodeComparedToNullpointer(IASTNode node, boolean notNegatedComparison) {
		if(node instanceof IASTBinaryExpression) {
			IASTBinaryExpression binaryExpression = (IASTBinaryExpression)node;
			int operator = notNegatedComparison ? IASTBinaryExpression.op_equals : IASTBinaryExpression.op_notequals; 
			return binaryExpression.getOperator() == operator && (isNullExpression(binaryExpression.getOperand1()) || isNullExpression(binaryExpression.getOperand2()));
		}
		return false;
	}
	
	private static boolean isNullExpression(IASTNode node) {
		if(node instanceof IASTLiteralExpression) {
			IASTLiteralExpression literalExpression = (IASTLiteralExpression)node;
			String literalValue = String.valueOf(literalExpression.getValue());
			return Constants.NULL_VALUES.contains(literalValue);
		}
		return false;
	}

	public static boolean isConstCStringParameterDeclaration(IASTParameterDeclaration parameter) {
		IASTDeclarator parameterDeclarator = parameter.getDeclarator();
		IASTDeclSpecifier parameterDeclSpecifier = parameter.getDeclSpecifier();
		return hasCStringType(parameterDeclSpecifier) && isArrayOrPointer(parameterDeclarator) && parameterDeclSpecifier.isConst();
	}
	
	public static boolean isCStringParameterDeclaration(IASTParameterDeclaration parameter) {
		IASTDeclarator parameterDeclarator = parameter.getDeclarator();
		IASTDeclSpecifier parameterDeclSpecifier = parameter.getDeclSpecifier();		
		return hasCStringType(parameterDeclSpecifier) && isArrayOrPointer(parameterDeclarator) && !parameterDeclSpecifier.isConst();
	}
	
	public static boolean isCStringType(IType type) {
		if(type instanceof IPointerType) {
			IPointerType pointer = (IPointerType)type;
			IType pointee = pointer.getType();
			if(pointee instanceof IBasicType) {
				IBasicType basicType = (IBasicType)pointee;
				return basicType.getKind() == Kind.eChar ||
					   basicType.getKind() == Kind.eWChar;
			}
		}
		return false;
	}
	
	public static boolean isStdStringParameterDeclaration(IASTParameterDeclaration parameter) {
		return hasStdStringType(parameter) || hasStdStringConstReferenceType(parameter);
	}
	
	public static boolean isStdStringType(IType type) {
		if(type instanceof ICPPReferenceType) {
			ICPPReferenceType referenceType = (ICPPReferenceType)type;
			if(referenceType.getType() instanceof IQualifierType) {
				IQualifierType qualifierType = (IQualifierType)referenceType.getType();
				if(qualifierType.isConst() && qualifierType.getType() instanceof ICPPClassType) {
					ICPPClassType classType = (ICPPClassType)qualifierType.getType();
					return classType.getName().equals(StdString.STRING);
				}
			}
		}
		else if(type instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType)type;
			return classType.getName().equals(StdString.STRING);
		}
		return false;
	}

	public static boolean isAssignedToCharPointer(IASTNode functionCall, boolean isConst) {
		IASTNode parent = functionCall.getParent();
		
		if(isAssignment(parent)) {
			IASTBinaryExpression assignment = (IASTBinaryExpression)parent;
			IASTExpression originalOperand1 = (IASTExpression)assignment.getOperand1().getOriginalNode();
			IType expressionType = originalOperand1.getExpressionType();
			if(expressionType instanceof IPointerType) {
				IPointerType pointerType = (IPointerType)expressionType;
				IType type = pointerType.getType();
				if(type instanceof IBasicType) {
					IBasicType basicType = (IBasicType)type;
					return (basicType.getKind() == Kind.eChar) && (isConst == pointerType.isConst());
				}
			}	
		}
		else if(parent instanceof IASTEqualsInitializer) {
			IASTNode possibleDeclarator = parent.getParent();
			if(possibleDeclarator instanceof IASTDeclarator) {
				IASTDeclarator declarator = (IASTDeclarator)possibleDeclarator;
				IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration)declarator.getParent()).getDeclSpecifier();
				return hasCStringType(declSpecifier) && isArrayOrPointer(declarator) && (declSpecifier.isConst() == isConst);
			}
		}
		return false;
	}

	public static boolean isConversionToCharPointer(IASTNode node, boolean isConst) {
		if(isConst) {
			return isCallToMemberFunction(node, StdString.C_STR);
		}
		else {
			if(isAddressOperatorExpression(node)) {
				IASTUnaryExpression addressOperatorExpression = (IASTUnaryExpression)node;
				if(isDereferenceExpression(addressOperatorExpression.getOperand())) {
					IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)addressOperatorExpression.getOperand();
					if(isCallToMemberFunction(dereferenceExpression.getOperand(), StdString.BEGIN)) {
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
		if(node.getParent() instanceof IASTBinaryExpression) {
			IASTBinaryExpression comparison = (IASTBinaryExpression)node.getParent();
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
				return comparedForEquality && (isCallToFunction(strlenOperand, CString.STRLEN) || isCallToMemberFunction(strlenOperand, StdString.SIZE));
			}
			else {
				return comparedForInequality && (isCallToFunction(strlenOperand, CString.STRLEN) || isCallToMemberFunction(strlenOperand, StdString.SIZE));
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
			result = (IASTIdExpression)fieldReference.getFieldOwner();
		}
		else if(isConversionToCharPointer(node, false)) {	//&str.begin()
			IASTUnaryExpression addressOperatorExpression = (IASTUnaryExpression)node;
			IASTUnaryExpression dereferenceExpression = (IASTUnaryExpression)addressOperatorExpression.getOperand();
			IASTFunctionCallExpression beginCall = (IASTFunctionCallExpression)dereferenceExpression.getOperand();
			IASTFieldReference fieldReference = (IASTFieldReference)beginCall.getFunctionNameExpression();
			result = (IASTIdExpression)fieldReference.getFieldOwner(); 
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
	
	public static boolean equalDeclSpecifiers(IASTDeclSpecifier declSpecifier1, IASTDeclSpecifier declSpecifier2) {
		boolean equalAttributes = declSpecifier1.getStorageClass() == declSpecifier2.getStorageClass() &&
				declSpecifier1.isConst() == declSpecifier2.isConst() &&
				declSpecifier1.isInline() == declSpecifier2.isInline() &&
				declSpecifier1.isRestrict() == declSpecifier2.isRestrict() &&
				declSpecifier1.isVolatile() == declSpecifier2.isVolatile();
		
		if(declSpecifier1 instanceof IASTNamedTypeSpecifier && declSpecifier2 instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier namedTypeSpecifier1 = (IASTNamedTypeSpecifier)declSpecifier1;
			IASTNamedTypeSpecifier namedTypeSpecifier2 = (IASTNamedTypeSpecifier)declSpecifier2;
			boolean equalTypes = namedTypeSpecifier1.getName().toString().equals(namedTypeSpecifier2.getName().toString());
			return equalAttributes && equalTypes;
		}
		else if(declSpecifier1 instanceof IASTSimpleDeclSpecifier && declSpecifier2 instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier1 = (IASTSimpleDeclSpecifier)declSpecifier1;
			IASTSimpleDeclSpecifier simpleDeclSpecifier2 = (IASTSimpleDeclSpecifier)declSpecifier2;
			boolean equalAdditionalAttributes = simpleDeclSpecifier1.isComplex() == simpleDeclSpecifier2.isComplex() &&
									simpleDeclSpecifier1.isImaginary() == simpleDeclSpecifier2.isImaginary() &&
									simpleDeclSpecifier1.isLong() == simpleDeclSpecifier2.isLong() &&
									simpleDeclSpecifier1.isLongLong() == simpleDeclSpecifier2.isLongLong() &&
									simpleDeclSpecifier1.isShort() == simpleDeclSpecifier2.isShort() &&
									simpleDeclSpecifier1.isSigned() == simpleDeclSpecifier2.isSigned() &&
									simpleDeclSpecifier1.isUnsigned() == simpleDeclSpecifier2.isUnsigned();		
			boolean equalTypes = simpleDeclSpecifier1.getType() == simpleDeclSpecifier2.getType();
			return equalAttributes && equalAdditionalAttributes && equalTypes;
		}
		return false;
	}
	
	public static boolean equalDeclarators(ICPPASTDeclarator declarator1, ICPPASTDeclarator declarator2) {
		IASTPointerOperator[] pointerOperators1 = declarator1.getPointerOperators();
		IASTPointerOperator[] pointerOperators2 = declarator2.getPointerOperators();
		if(pointerOperators1.length != pointerOperators2.length) {
			return false;
		}
		
		for(int i = 0; i < pointerOperators1.length; ++i) {
			if(!pointerOperators1[i].getRawSignature().equals(pointerOperators2[i].getRawSignature())) {
				return false;
			}
		}
		return true;
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
	
	public static IASTIdExpression findFirstIdExpression(IASTName name, IASTNode node) {
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
	
	public static boolean isEmptyReturnStatement(IASTNode node) {
		if(node instanceof IASTReturnStatement) {
			IASTReturnStatement returnStatement = (IASTReturnStatement)node;
			return returnStatement.getReturnValue() == null;
		}
		return false;
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
	
	public static IASTNode findGuardClause(IASTName name, IASTStatement[] statements) {
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
		for(IASTStatement statement : statements) {
			if(statement instanceof IASTDeclarationStatement) {
				IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement)statement;
				IASTDeclaration declaration = declarationStatement.getDeclaration();
				if(declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration;
					for(IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
						if(declarator.getName().resolveBinding().equals(name.resolveBinding())) {
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
		while(node != null && !isCondition(node) && !isAssignedToBoolean(node) && !isAssert(node)) {
			node = node.getParent();
		}
		return node;
	}
	
	public static IType getParameterType(IASTNode idExpression) {
		idExpression = idExpression.getOriginalNode();
		IASTNode parent = idExpression.getParent();
		int argIndex = -1;
		ICPPFunction functionBinding = null;
		
		if(parent instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression functionCall = (ICPPASTFunctionCallExpression)parent;
			argIndex = Arrays.asList(functionCall.getArguments()).indexOf(idExpression);
			IASTName functionName = ((IASTIdExpression)functionCall.getFunctionNameExpression()).getName();
			IBinding functionNameBinding = functionName.resolveBinding();
			
			if(functionNameBinding instanceof ICPPClassType) {
				IASTImplicitName implicitNames[] = functionCall.getImplicitNames();
				if(implicitNames.length > 0) {
					IBinding binding = implicitNames[0].resolveBinding();
					if(binding instanceof ICPPConstructor) {
						functionBinding = (ICPPFunction)binding;
					}	
				}
			}
			else if(functionNameBinding instanceof ICPPFunction) {
				functionBinding = (ICPPFunction)functionNameBinding;
			}
		}
		else if(parent instanceof ICPPASTConstructorInitializer) {
			ICPPASTConstructorInitializer constructorInitializer = (ICPPASTConstructorInitializer)parent;
			argIndex = Arrays.asList(constructorInitializer.getArguments()).indexOf(idExpression);
			IASTImplicitNameOwner declarator = (IASTImplicitNameOwner)parent.getParent();
			IASTImplicitName implicitNames[] = declarator.getImplicitNames();
			if(implicitNames.length > 0) {
				functionBinding = (ICPPConstructor)implicitNames[0].resolveBinding();
			}
		}
		else if(parent instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList initializerList = (ICPPASTInitializerList)parent;
			argIndex = Arrays.asList(initializerList.getClauses()).indexOf(idExpression);
			
			IASTImplicitNameOwner declarator = null;
			if(parent.getParent() instanceof IASTDeclarator || parent.getParent() instanceof ICPPASTNewExpression) {
				declarator = (IASTImplicitNameOwner)parent.getParent();
				IASTImplicitName implicitNames[] = declarator.getImplicitNames();
				if(implicitNames.length > 0) {
					functionBinding = (ICPPConstructor)implicitNames[0].resolveBinding();
				}
			}
		}
		
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
		boolean isLValue = ASTAnalyzer.isLValueInAssignment(idExpression) && ((IASTBinaryExpression)idExpression.getParent()).getOperand2() instanceof IASTBinaryExpression;
		boolean isPlusAssigned = ASTAnalyzer.isPlusAssignment(idExpression.getParent()) && ((IASTBinaryExpression)idExpression.getParent()).getOperand1() == idExpression;
		boolean isIncremented = ASTAnalyzer.isUnaryExpression(idExpression.getParent(), IASTUnaryExpression.op_prefixIncr) || ASTAnalyzer.isUnaryExpression(idExpression.getParent(), IASTUnaryExpression.op_postFixIncr);
		return isLValue || isPlusAssigned || isIncremented;
	}
}