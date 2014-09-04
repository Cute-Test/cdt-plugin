package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.FunctionDescription.Sentinel;

public class ComparisonTransformer extends Transformer  {
	private Mapping mapping;
	private boolean isEqual;
	
	public ComparisonTransformer(Context context, IASTIdExpression idExpression, Mapping mapping, boolean isEqual) {
		super(context, idExpression,  ASTAnalyzer.getEnclosingBoolean(idExpression));
		this.mapping = mapping;
		this.isEqual = isEqual;
	}
	
	@Override
	public void transform(ASTChangeDescription changeDescription) {
		changeDescription.addHeaderToInclude(mapping.getOutFunction().getHeader());
		super.transform(changeDescription);
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		IASTFunctionCallExpression outFunctionCall = createOutFunctionCall();
		IASTExpression sentinel = createSentinel();
		return ExtendedNodeFactory.newEqualityComparison(outFunctionCall, sentinel, isEqual);
	}
	
	private IASTFunctionCallExpression createOutFunctionCall() { 
		String outFunctionName = mapping.getOutFunction().getName();
		IASTNode outArguments[] = getOutArguments();
		
		if(mapping.getOutFunction().isMemberFunction()) {
			return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, outFunctionName, outArguments);
		}
		else {
			return ExtendedNodeFactory.newFunctionCallExpression(outFunctionName, outArguments);
		}
	}
	
	private IASTExpression createSentinel() {
		Sentinel outFunctionSentinel = mapping.getOutFunction().getSentinel();
		IASTExpression sentinel = null;
		
		if(outFunctionSentinel == Sentinel.NPOS) {
			sentinel = ExtendedNodeFactory.newNposExpression();
		}
		else if(outFunctionSentinel == Sentinel.END) {
			IASTNode outArguments[] = getOutArguments();
			return (IASTExpression)outArguments[1];
		}
		
		return sentinel;
	}
	
	private IASTNode[] getOutArguments() {
		String inFunctionName = mapping.getInFunction().getName();
		IASTFunctionCallExpression inFunctionCall = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunctionName); 
		return mapping.getArgumentMapping().getOutArguments(inFunctionCall.getArguments(), idExpression, context);
	}
}
