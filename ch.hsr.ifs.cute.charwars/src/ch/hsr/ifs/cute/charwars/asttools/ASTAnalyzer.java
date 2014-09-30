package ch.hsr.ifs.cute.charwars.asttools;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;

import ch.hsr.ifs.cute.charwars.asttools.FindIdExpressionsVisitor;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.LiteralAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.UEAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;

public class ASTAnalyzer {
	public static boolean isFunctionDefinitionParameterDeclaration(IASTParameterDeclaration declaration) {
		return declaration.getParent().getParent() instanceof IASTFunctionDefinition;
	}
	
	public static boolean isLValueInAssignment(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		return BEAnalyzer.isAssignment(parent) && BEAnalyzer.isOp1(idExpression);
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
		for(IASTNode cn = idExpression; cn != null && !UEAnalyzer.isDereferenceExpression(cn); cn = cn.getParent()) {
			if(BEAnalyzer.isDivision(cn)) {
				return isSizeOfDivision(cn); 
			}
		};
		return false;
	}
	
	public static boolean isStringLengthCalculation(IASTIdExpression idExpression) {
		if(UEAnalyzer.isDereferenceExpression(idExpression.getParent()))
			return false;
		
		IASTNode currentNode = idExpression;
		while(currentNode != null && !BEAnalyzer.isSubtraction(currentNode)) {
			currentNode = currentNode.getParent();
		}
		
		if(currentNode == null)
			return false;
		
		IASTNode minuend = BEAnalyzer.getOperand1(currentNode);
		IASTNode subtrahend = BEAnalyzer.getOperand2(currentNode);
		return isSizeOfDivision(minuend) && LiteralAnalyzer.isInteger(subtrahend, 1);
	}
	
	private static boolean isSizeOfDivision(IASTNode node) {
		if(BEAnalyzer.isDivision(node)) {
			IASTNode dividend = BEAnalyzer.getOperand1(node);
			IASTNode divisor = BEAnalyzer.getOperand2(node);
			return UEAnalyzer.isSizeofExpression(dividend) && UEAnalyzer.isSizeofExpression(divisor);
		}
		return false;
	}
	
	public static IASTStatement getStatement(IASTNode node) {
		IASTNode result = node;
		while(result != null && !(result instanceof IASTStatement)) {
			result = result.getParent();
		}
		return (IASTStatement)result;
	}
	
	public static boolean isConversionToCharPointer(IASTNode node) {
		return isConversionToCharPointer(node, true) || isConversionToCharPointer(node, false);
	}

	public static boolean isConversionToCharPointer(IASTNode node, boolean isConst) {
		if(isConst) {
			return FunctionAnalyzer.isCallToMemberFunction(node, Function.C_STR);
		}
		else {
			if(UEAnalyzer.isAddressOperatorExpression(node)) {
				IASTNode operand = UEAnalyzer.getOperand(node);
				if(UEAnalyzer.isDereferenceExpression(operand)) {
					IASTNode dereferencedNode = UEAnalyzer.getOperand(operand);
					return FunctionAnalyzer.isCallToMemberFunction(dereferencedNode, Function.BEGIN);
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
			IASTFunctionCallExpression beginCall = (IASTFunctionCallExpression)UEAnalyzer.getOperand(UEAnalyzer.getOperand(node));
			IASTFieldReference fieldReference = (IASTFieldReference)beginCall.getFunctionNameExpression();
			result = fieldReference.getFieldOwner(); 
		}

		return result;
	}
	
	public static boolean isLeftShiftExpressionToStdCout(IASTNode node) {
		if(BEAnalyzer.isLeftShiftExpression(node) && node instanceof ICPPASTBinaryExpression) {
			IASTExpression leftOperand = BEAnalyzer.getOperand1(node);
			return isStdCout(leftOperand) || isLeftShiftExpressionToStdCout(leftOperand);
		}
		return false;
	}
	
	private static boolean isStdCout(IASTNode node) {
		if(node instanceof IASTIdExpression) {
			IASTName name = ((IASTIdExpression)node.getOriginalNode()).getName();
			String nameStr = name.getRawSignature();
			return nameStr.equals(Constants.STD_COUT) || nameStr.equals(Constants.COUT);
		}
		return false;
	}
	
	public static IASTNode getMarkedNode(IASTTranslationUnit ast, int offset, int length) {
		return ast.getNodeSelector(null).findNode(offset, length);
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
		
	public static boolean isDereferencedToChar(IASTNode node) {
		IASTNode parent = node.getParent();
		if(UEAnalyzer.isBracketExpression(parent) || BEAnalyzer.isAddition(parent)) {
			return isDereferencedToChar(parent);
		}
		return UEAnalyzer.isDereferenceExpression(parent);
	}
	
	public static IASTStatement getTopLevelParentStatement(IASTNode node) {
		IASTStatement lastStatement = null;
		for(IASTNode cn = node; cn != null && !(cn instanceof IASTFunctionDefinition); cn = cn.getParent()) {
			if(cn instanceof IASTStatement && !(cn instanceof IASTCompoundStatement)) { 
				lastStatement = (IASTStatement)cn;
			}
		}
		return lastStatement;
	}
	
	public static boolean modifiesCharPointer(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		boolean isLValue = isLValueInAssignment(idExpression) && BEAnalyzer.getOperand2(parent) instanceof IASTBinaryExpression;
		boolean isPlusAssigned = BEAnalyzer.isPlusAssignment(parent) && BEAnalyzer.isOp1(idExpression);
		boolean isIncremented = UEAnalyzer.isIncrementation(parent);
		return isLValue || isPlusAssigned || isIncremented;
	}
	
	public static boolean isIndexCalculation(IASTIdExpression idExpression) {
		IASTNode parent = idExpression.getParent();
		if(BEAnalyzer.isSubtraction(parent)) {
			return BEAnalyzer.isOp1(idExpression) && isConversionToCharPointer(BEAnalyzer.getOtherOperand(idExpression), true);
		}
		return false;
	}
}