package ch.hsr.ifs.cute.charwars.asttools;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.cute.charwars.constants.StdString;

public class ExtendedNodeFactory {
	public final static ICPPNodeFactory factory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
	
	public static IASTFunctionCallExpression newFunctionCallExpression(String functionName, IASTNode... args) {
		IASTIdExpression function = newIdExpression(functionName);
		return factory.newFunctionCallExpression(function, getArgArray(args));
	}
	
	public static IASTFunctionCallExpression newMemberFunctionCallExpression(IASTName objectName, String methodName, IASTNode... args) {
		IASTFieldReference fieldReference = factory.newFieldReference(newName(methodName), factory.newIdExpression(objectName.copy()));
		return factory.newFunctionCallExpression(fieldReference, getArgArray(args));
	}
	
	private static IASTInitializerClause[] getArgArray(IASTNode... args) {
		ArrayList<IASTInitializerClause> argList = new ArrayList<IASTInitializerClause>();
		for(IASTNode arg : args) {
			argList.add((IASTInitializerClause)arg);
		}
		return argList.toArray(new IASTInitializerClause[]{});
	}
	
	public static IASTLiteralExpression newIntegerLiteral(int number) {
		return factory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, String.valueOf(number));
	}
	
	public static IASTLiteralExpression newStringLiteral(String str) {
		return factory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, str);
	}
	
	public static IASTBinaryExpression newAssignment(IASTExpression lhs, IASTExpression rhs) {
		return factory.newBinaryExpression(IASTBinaryExpression.op_assign, lhs, rhs);
	}
	
	public static IASTBinaryExpression newPlusAssignment(IASTExpression lhs, IASTExpression rhs) {
		return factory.newBinaryExpression(IASTBinaryExpression.op_plusAssign, lhs, rhs);
	}
	
	public static IASTBinaryExpression newPlusExpression(IASTExpression lhs, IASTExpression rhs) {
		return factory.newBinaryExpression(IASTBinaryExpression.op_plus, lhs, rhs);
	}
	
	public static IASTBinaryExpression newMinusExpression(IASTExpression lhs, IASTExpression rhs) {
		return factory.newBinaryExpression(IASTBinaryExpression.op_minus, lhs, rhs);
	}
	
	public static IASTUnaryExpression newLogicalNotExpression(IASTExpression operand) {
		return factory.newUnaryExpression(IASTUnaryExpression.op_not, operand);
	}
	
	public static IASTBinaryExpression newEqualityComparison(IASTExpression lhs, IASTExpression rhs, boolean isEqual) {
		int op = isEqual ? IASTBinaryExpression.op_equals : IASTBinaryExpression.op_notequals;
		return factory.newBinaryExpression(op, lhs, rhs);
	}
	
	public static IASTExpression newNposExpression() {
		ICPPASTQualifiedName std_string_npos = factory.newQualifiedName((ICPPASTName)newName(StdString.STD));
		std_string_npos.addName(newName(StdString.STRING));
		std_string_npos.addName(newName(StdString.NPOS));
		return factory.newIdExpression(std_string_npos);
	}
	
	public static IASTUnaryExpression newDereferenceOperatorExpression(IASTExpression expression) {
		return factory.newUnaryExpression(IASTUnaryExpression.op_star, expression);
	}
	
	public static IASTUnaryExpression newAdressOperatorExpression(IASTExpression expression) {
		return factory.newUnaryExpression(IASTUnaryExpression.op_amper, expression);
	}
	
	public static IASTUnaryExpression newNegatedExpression(IASTExpression expression) {
		return factory.newUnaryExpression(IASTUnaryExpression.op_minus, expression);
	}
	
	public static IASTInitializer newEqualsInitializer(IASTInitializerClause expression) {
		return factory.newEqualsInitializer(expression);
	}
	
	public static IASTDeclarationStatement newDeclarationStatement(String type, String varName, IASTInitializerClause initializerClause) {
		IASTDeclSpecifier declSpecifier = factory.newTypedefNameSpecifier(newName(type));
		IASTSimpleDeclaration simpleDeclaration = factory.newSimpleDeclaration(declSpecifier);
		IASTDeclarator declarator = factory.newDeclarator(newName(varName));
		IASTInitializer initializer = factory.newEqualsInitializer(initializerClause);
		declarator.setInitializer(initializer);
		simpleDeclaration.addDeclarator(declarator);
		return factory.newDeclarationStatement(simpleDeclaration);
	}
	
	public static IASTConditionalExpression newConditionalExpression(IASTExpression condition, IASTExpression positive, IASTExpression negative) {
		return factory.newConditionalExpession(condition, positive, negative);
	}
	
	public static IASTIdExpression newIdExpression(String name) {
		return factory.newIdExpression(newName(name));
	}
	
	public static IASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript) {
		return factory.newArraySubscriptExpression(arrayExpr, subscript);
	}
	
	public static IASTCompoundStatement newCompoundStatement(IASTStatement... statements) {
		IASTCompoundStatement compoundStatement = factory.newCompoundStatement();
		for(IASTStatement statement : statements) {
			compoundStatement.addStatement(statement);
		}
		return compoundStatement;
	}
	
	public static IASTIfStatement newIfStatement(IASTExpression condition, IASTCompoundStatement then) {
		return factory.newIfStatement(condition, then, null);
	}
	
	public static IASTExpressionStatement newExpressionStatement(IASTExpression expression) {
		return factory.newExpressionStatement(expression);
	}
	
	public static IASTDeclarationStatement newDeclarationStatementFromDeclarator(IASTDeclarator declarator) {
		IASTDeclSpecifier newDeclSpecifier = ((IASTSimpleDeclaration)declarator.getParent()).getDeclSpecifier().copy();
		IASTSimpleDeclaration newDeclaration = factory.newSimpleDeclaration(newDeclSpecifier);
		IASTDeclarator newDeclarator = declarator.copy();
		newDeclaration.addDeclarator(newDeclarator);
		return factory.newDeclarationStatement(newDeclaration);
	}
	
	public static IASTName newName(String name) {
		return factory.newName(name.toCharArray());
	}
	
	public static IASTUnaryExpression newBracketedExpression(IASTExpression operand) {
		return factory.newUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, operand);
	}
	
	public static IASTFieldReference newFieldReference(IASTName name, IASTExpression owner) {
		return factory.newFieldReference(name, owner);
	}
	
	public static ICPPASTNamedTypeSpecifier newNamedTypeSpecifier(String typeName) {
		return factory.newTypedefNameSpecifier(newName(typeName));
	}
	
	public static ICPPASTDeclarator newDeclarator(String name) {
		return factory.newDeclarator(newName(name));
	}
	
	public static ICPPASTDeclarator newReferenceDeclarator(String name) {
		ICPPASTDeclarator declarator = newDeclarator(name);
		declarator.addPointerOperator(factory.newReferenceOperator(false));
		return declarator;
	}
	
	public static IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration) {
		return factory.newDeclarationStatement(declaration);
	}
	
	public static IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		return factory.newSimpleDeclaration(declSpecifier);
	}
	
	public static ICPPASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		return factory.newParameterDeclaration(declSpecifier, declarator);
	}
	
	public static IASTReturnStatement newReturnStatement(IASTExpression returnValue) {
		return factory.newReturnStatement(returnValue);
	}
}
