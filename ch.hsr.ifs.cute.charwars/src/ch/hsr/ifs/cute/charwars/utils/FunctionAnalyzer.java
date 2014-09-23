package ch.hsr.ifs.cute.charwars.utils;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.constants.Function;

public class FunctionAnalyzer {
	public static boolean hasOffset(IASTIdExpression idExpression, Function function) {
		if(isPartOfFunctionCallArg(idExpression, 0, function)) {
			return getEnclosingFunctionCall(idExpression, function) != idExpression.getParent();
		}
		return false;
	}
	
	public static boolean isPartOfFunctionCallArg(IASTNode node, int argIndex, Function function) {
		IASTNode lastNode = node;
		IASTNode currentNode = lastNode.getParent();
		while(!isCallToFunction(currentNode, function) && (BEAnalyzer.isSubtraction(currentNode) || BEAnalyzer.isAddition(currentNode))) {
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
	
	public static IASTFunctionCallExpression getEnclosingFunctionCall(IASTNode startNode, Function function) {
		IASTNode currentNode = startNode;
		while(!isCallToFunction(currentNode, function)) {
			currentNode = currentNode.getParent();
		}
		return (IASTFunctionCallExpression)currentNode;
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
}
