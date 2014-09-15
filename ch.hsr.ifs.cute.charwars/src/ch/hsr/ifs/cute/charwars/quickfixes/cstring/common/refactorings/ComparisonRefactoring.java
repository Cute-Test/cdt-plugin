package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Function.Sentinel;

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
		if(canHandleOffsets() && (ASTAnalyzer.isOffset(idExpression, context) || ASTAnalyzer.hasOffset(idExpression, inFunction))) {
			if(ASTAnalyzer.isPartOfFunctionCallArg(idExpression, 0, inFunction)) {
				IASTNode functionCall = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunction);
				prepare(idExpression, functionCall, context);
			}
		}
		else if(!context.isOffset(idExpression)) {
			if(ASTAnalyzer.isFunctionCallArg(idExpression, 0, inFunction)) {
				prepare(idExpression, idExpression.getParent(), context);
			}
		}
	}
	
	private void prepare(IASTIdExpression idExpression, IASTNode node, Context context) {
		Sentinel inFunctionSentinel = inFunction.getSentinel();
		
		if(inFunctionSentinel == Sentinel.NULL) {
			if(ASTAnalyzer.isCheckedIfEqualToNull(node)) {
				isApplicable = true;
				config.put(NODE_TO_REPLACE, ASTAnalyzer.getEnclosingBoolean(idExpression));
				config.put(IS_EQUAL, true);
			}
			else if(ASTAnalyzer.isCheckedIfNotEqualToNull(node)) {
				isApplicable = true;
				config.put(NODE_TO_REPLACE, ASTAnalyzer.getEnclosingBoolean(idExpression));
				config.put(IS_EQUAL, false);
			}
		}
		else if(inFunctionSentinel == Sentinel.STRLEN) {
			if(ASTAnalyzer.isNodeComparedToStrlen(node, true)) {
				isApplicable = true;
				config.put(NODE_TO_REPLACE, ASTAnalyzer.getEnclosingBoolean(idExpression));
				config.put(IS_EQUAL, true);
			}
			else if(ASTAnalyzer.isNodeComparedToStrlen(node, false)) {
				isApplicable = true;
				config.put(NODE_TO_REPLACE, ASTAnalyzer.getEnclosingBoolean(idExpression));
				config.put(IS_EQUAL, false);
			}
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTFunctionCallExpression outFunctionCall = createOutFunctionCall(idExpression, context);
		IASTExpression sentinel = createSentinel(idExpression, context);
		return ExtendedNodeFactory.newEqualityComparison(outFunctionCall, sentinel, (boolean)config.get(IS_EQUAL));
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
		IASTFunctionCallExpression inFunctionCall = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunction); 
		return argMapping.getOutArguments(inFunctionCall.getArguments(), idExpression, context);
	}
	
	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setStatementHasChanged(true);
		changeDescription.addHeaderToInclude(outFunction.getHeader());
	}
}
