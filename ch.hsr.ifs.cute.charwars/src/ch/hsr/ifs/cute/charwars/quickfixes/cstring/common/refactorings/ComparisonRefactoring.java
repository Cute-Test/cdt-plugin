package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.Function.Sentinel;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.BoolAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.UEAnalyzer;

public class ComparisonRefactoring extends Refactoring {
	private static final String IS_EQUAL = "IS_EQUAL";
	private Function inFunction;
	private Function outFunction;
	private ArgMapping argMapping;
	
	public ComparisonRefactoring(Function inFunction, Function outFunction, ArgMapping argMapping, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		this.argMapping = argMapping;
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		if(canHandleOffsets() && (context.isOffset(idExpression) || FunctionAnalyzer.hasOffset(idExpression, inFunction))) {
			if(FunctionAnalyzer.isPartOfFunctionCallArg(idExpression, 0, inFunction)) {
				IASTNode functionCall = FunctionAnalyzer.getEnclosingFunctionCall(idExpression, inFunction);
				prepare(idExpression, functionCall, context);
			}
		}
		else if(!context.isOffset(idExpression)) {
			if(FunctionAnalyzer.isFunctionCallArg(idExpression, 0, inFunction)) {
				prepare(idExpression, idExpression.getParent(), context);
			}
		}
	}
	
	private void makeApplicable(IASTNode nodeToReplace, boolean isEqual) {
		super.makeApplicable(nodeToReplace);
		config.put(IS_EQUAL, isEqual);
	}
	
	private void prepare(IASTIdExpression idExpression, IASTNode node, Context context) {
		Sentinel inFunctionSentinel = inFunction.getSentinel();
		IASTNode nodeToReplace = BoolAnalyzer.getEnclosingBoolean(idExpression);
		
		if(inFunctionSentinel == Sentinel.NULL) {
			if(CheckAnalyzer.isNodeComparedToNull(node, true)) {
				makeApplicable(nodeToReplace, true);
			}
			else if(CheckAnalyzer.isNodeComparedToNull(node, false)) {
				makeApplicable(nodeToReplace, false);
			}
		}
		else if(inFunctionSentinel == Sentinel.STRLEN) {
			if(CheckAnalyzer.isNodeComparedToStrlen(node, true)) {
				makeApplicable(nodeToReplace, true);
			}
			else if(CheckAnalyzer.isNodeComparedToStrlen(node, false)) {
				makeApplicable(nodeToReplace, false);
			}
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTExpression lhs = createLhs(idExpression, context);
		IASTExpression sentinel = createSentinel(idExpression, context);
		return ExtendedNodeFactory.newEqualityComparison(lhs, sentinel, (boolean)config.get(IS_EQUAL));
	}
	
	private IASTExpression createLhs(IASTIdExpression idExpression, Context context) {
		IASTNode nodeToReplace = (IASTNode)config.get(NODE_TO_REPLACE);
		IASTExpression functionCall = createOutFunctionCall(idExpression, context);
		
		if(BEAnalyzer.isComparison(nodeToReplace)) {
			IASTNode op1 = BEAnalyzer.getOperand1(nodeToReplace);
			IASTNode op2 = BEAnalyzer.getOperand2(nodeToReplace);
			if(!FunctionAnalyzer.isCallToFunction(op1, inFunction) && !FunctionAnalyzer.isCallToFunction(op2, inFunction)) {
				IASTNode assignment = UEAnalyzer.isBracketExpression(op1) ? UEAnalyzer.getOperand(op1) : UEAnalyzer.getOperand(op2); 
				IASTNode lvalue = BEAnalyzer.getOperand1(assignment);
				IASTExpression newAssignment = ExtendedNodeFactory.newAssignment((IASTExpression)lvalue.copy(), functionCall);
				return ExtendedNodeFactory.newBracketedExpression(newAssignment);
			}
		}
		return functionCall;
	}
	
	private IASTFunctionCallExpression createOutFunctionCall(IASTIdExpression idExpression, Context context) {
		String outFunctionName = outFunction.getName();
		IASTNode outArguments[] = getOutArguments(idExpression, context);
		IASTName stringName = ExtendedNodeFactory.newName(context.getStringVarName());
		
		if(outFunction.isMemberFunction()) {
			return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, outFunctionName, outArguments);
		}
		else {
			return ExtendedNodeFactory.newFunctionCallExpression(outFunctionName, outArguments);
		}
	}
	
	private IASTExpression createSentinel(IASTIdExpression idExpression, Context context) {
		Sentinel outFunctionSentinel = outFunction.getSentinel();
		IASTExpression sentinel = null;
		
		if(outFunctionSentinel == Sentinel.NPOS) {
			sentinel = ExtendedNodeFactory.newNposExpression(context.getStringType());
		}
		else if(outFunctionSentinel == Sentinel.END) {
			IASTNode outArguments[] = getOutArguments(idExpression, context);
			return (IASTExpression)outArguments[1];
		}
		
		return sentinel;
	}
	
	private IASTNode[] getOutArguments(IASTIdExpression idExpression, Context context) {
		IASTFunctionCallExpression inFunctionCall = FunctionAnalyzer.getEnclosingFunctionCall(idExpression, inFunction); 
		return argMapping.getOutArguments(inFunctionCall.getArguments(), idExpression, context);
	}
	
	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setStatementHasChanged(true);
		changeDescription.addHeaderToInclude(outFunction.getHeader());
	}
}
