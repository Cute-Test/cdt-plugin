package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class OperatorRefactoring extends Refactoring {
	private Function inFunction;
	private Function outFunction;
	
	public OperatorRefactoring(Function inFunction, Function outFunction, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		this.contextStates = new HashSet<ContextState>();
		for(ContextState contextState : contextStates) {
			this.contextStates.add(contextState);
		}
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		String inFunctionName = inFunction.getName();
		String outFunctionName = outFunction.getName();
		boolean isStrcmp = inFunctionName.equals(CString.STRCMP);
		
		boolean isOther = !isStrcmp && ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunctionName);
		boolean isStringEqualityCheck = isStrcmp && outFunctionName.equals(StdString.OP_EQUALS) && ASTAnalyzer.isPartOfStringEqualityCheck(idExpression); 
		boolean isStringInequalityCheck = isStrcmp && outFunctionName.equals(StdString.OP_NOT_EQUALS) && ASTAnalyzer.isPartOfStringInequalityCheck(idExpression);
		
		if(!context.isOffset(idExpression) && (isOther || isStringEqualityCheck || isStringInequalityCheck)) {
			IASTNode nodeToReplace = isStrcmp ? ASTAnalyzer.getEnclosingBoolean(idExpression) : idExpression.getParent();
			isApplicable = true;
			config.put(NODE_TO_REPLACE, nodeToReplace);
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)idExpression.getParent();
		IASTInitializerClause[] args = functionCall.getArguments();
		String outFunctionName = outFunction.getName();
		IASTExpression lhs = idExpression;
		
		if(outFunctionName.equals(StdString.OP_ASSIGNMENT)) {
			IASTExpression rhs = (IASTExpression)ASTAnalyzer.extractStdStringArg(args[1]);
			return ExtendedNodeFactory.newAssignment(lhs, rhs);
		}
		else if(outFunctionName.equals(StdString.OP_PLUS_ASSIGNMENT)) {
			IASTExpression rhs = (IASTExpression)ASTAnalyzer.extractStdStringArg(args[1]);
			return ExtendedNodeFactory.newPlusAssignment(lhs, rhs);
		}
		else if(outFunctionName.equals(StdString.OP_EQUALS)) {
			IASTExpression rhs = (IASTExpression)(args[0] == idExpression ? args[1] : args[0]);
			return ExtendedNodeFactory.newEqualityComparison(lhs, rhs, true);
		}
		else if(outFunctionName.equals(StdString.OP_NOT_EQUALS)) {
			IASTExpression rhs = (IASTExpression)(args[0] == idExpression ? args[1] : args[0]);
			return ExtendedNodeFactory.newEqualityComparison(lhs, rhs, false);	
		}
		return null;
	}
}
