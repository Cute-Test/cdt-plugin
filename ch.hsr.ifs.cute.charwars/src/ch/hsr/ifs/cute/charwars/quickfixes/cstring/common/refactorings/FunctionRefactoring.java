package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.FunctionTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class FunctionRefactoring extends Refactoring {
	private Mapping mapping;
	
	public FunctionRefactoring(Mapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		String inFunctionName = mapping.getInFunction().getName();
		
		if(mapping.isOffsetAllowed()) {
			if(ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, inFunctionName) || 
			   ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, Constants.STD_PREFIX + inFunctionName)) {
				IASTNode nodeToReplace = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunctionName);
				transformer = new FunctionTransformer(context, idExpression, nodeToReplace, mapping);
			}
		}
		else if(!context.isPotentiallyModifiedCharPointer(idExpression)) {
			if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunctionName) ||
			   ASTAnalyzer.isFunctionCallArgument(idExpression, 0, Constants.STD_PREFIX + inFunctionName)) {
				transformer = new FunctionTransformer(context, idExpression, idExpression.getParent(), mapping);
			}
		}

		return transformer;
	}
}
