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
		
		if(!mapping.isApplicableForContextState(context.getContextState())) {
			return null;
		}
		
		if(mapping.canHandleOffsets() && (ASTAnalyzer.isOffset(idExpression, context) || ASTAnalyzer.hasOffset(idExpression, context, mapping))) {
			transformer = createOffsetTransformer(idExpression, context);
		}
		else if(!context.isOffset(idExpression)) {
			transformer = createNormalTransformer(idExpression, context);
		}

		return transformer;
	}
	
	private Transformer createOffsetTransformer(IASTIdExpression idExpression, Context context) {
		String inFunctionName = mapping.getInFunction().getName();
		if(ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, inFunctionName) || 
		   ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, Constants.STD_PREFIX + inFunctionName)) {
			IASTNode nodeToReplace = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunctionName);
			return new FunctionTransformer(context, idExpression, nodeToReplace, mapping);
		}
		return null;
	}
	
	private Transformer createNormalTransformer(IASTIdExpression idExpression, Context context) {
		String inFunctionName = mapping.getInFunction().getName();
		if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunctionName) ||
		   ASTAnalyzer.isFunctionCallArgument(idExpression, 0, Constants.STD_PREFIX + inFunctionName)) {
			return new FunctionTransformer(context, idExpression, idExpression.getParent(), mapping);
		}
		return null;
	}
}
