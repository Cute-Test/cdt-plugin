package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;

public class FunctionTransformer extends Transformer {
	private Mapping mapping;
	
	public FunctionTransformer(Context context, IASTIdExpression idExpression, IASTNode nodeToReplace, Mapping mapping) {
		super(context, idExpression, nodeToReplace);
		this.mapping = mapping;
	}
	
	@Override
	public void transform(ASTChangeDescription changeDescription) {
		changeDescription.addHeaderToInclude(mapping.getOutFunction().getHeader());
		super.transform(changeDescription);
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		String outFunctionName = mapping.getOutFunction().getName();
		boolean isMemberFunction = mapping.getOutFunction().isMemberFunction();
		
		IASTFunctionCallExpression inFunctionCall = (IASTFunctionCallExpression)nodeToReplace;
		IASTNode adaptedArguments[] = mapping.getArgumentMapping().getOutArguments(inFunctionCall.getArguments(), idExpression, context);
		
		if(isMemberFunction) {
			IASTFunctionCallExpression memberFunctionCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, outFunctionName, adaptedArguments);	
			
			//special case for strlen() / wcslen()
			if(outFunctionName.equals(StdString.SIZE) && (idExpression.getParent() != nodeToReplace || context.isPotentiallyModifiedCharPointer(idExpression))) {
				IASTNode offset = ASTAnalyzer.getOffset(idExpression, context);
				return ExtendedNodeFactory.newBracketedExpression(ExtendedNodeFactory.newMinusExpression(memberFunctionCall, (IASTExpression)offset));
			}
				
			return memberFunctionCall;	
		}
		else {
			return ExtendedNodeFactory.newFunctionCallExpression(outFunctionName, adaptedArguments);
		}
	}
}
