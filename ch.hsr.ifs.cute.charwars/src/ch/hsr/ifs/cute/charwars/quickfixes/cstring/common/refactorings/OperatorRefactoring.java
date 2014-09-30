package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.BoolAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;

public class OperatorRefactoring extends Refactoring {
	private Function inFunction;
	private Function outFunction;
	
	public OperatorRefactoring(Function inFunction, Function outFunction, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		String outFunctionName = outFunction.getName();
		boolean isStrcmpOrWcscmp = (inFunction == Function.STRCMP) || (inFunction == Function.WCSCMP);
		boolean isOther = !isStrcmpOrWcscmp && FunctionAnalyzer.isFunctionCallArg(idExpression, 0, inFunction);
		boolean isStringEqualityCheck = isStrcmpOrWcscmp && outFunctionName.equals(StdString.OP_EQUALS) && CheckAnalyzer.isPartOfStringCheck(idExpression, true); 
		boolean isStringInequalityCheck = isStrcmpOrWcscmp && outFunctionName.equals(StdString.OP_NOT_EQUALS) && CheckAnalyzer.isPartOfStringCheck(idExpression, false);
		
		if(!context.isOffset(idExpression) && (isOther || isStringEqualityCheck || isStringInequalityCheck)) {
			IASTNode nodeToReplace = isStrcmpOrWcscmp ? BoolAnalyzer.getEnclosingBoolean(idExpression) : idExpression.getParent();
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
