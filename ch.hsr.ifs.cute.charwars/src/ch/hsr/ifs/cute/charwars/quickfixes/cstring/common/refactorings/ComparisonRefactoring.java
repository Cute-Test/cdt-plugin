package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Function.Sentinel;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.ComparisonTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class ComparisonRefactoring extends Refactoring {
	private Mapping mapping;
	
	public ComparisonRefactoring(Mapping mapping) {
		this.mapping = mapping;
	}
	
	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		String inFunctionName = mapping.getInFunction().getName();
		
		if(!mapping.isApplicableForContextState(context.getContextState())) {
			return null;
		}
		
		if(mapping.canHandleOffsets() && (ASTAnalyzer.isOffset(idExpression, context) || ASTAnalyzer.hasOffset(idExpression, context, mapping))) {
			if(ASTAnalyzer.isPartOfFunctionCallArgument(idExpression, 0, inFunctionName)) {
				IASTNode functionCall = ASTAnalyzer.getEnclosingFunctionCall(idExpression, inFunctionName);
				transformer = createComparisonTransformer(idExpression, functionCall, context);
			}
		}
		else if(!context.isOffset(idExpression)) {
			if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunctionName)) {
				transformer = createComparisonTransformer(idExpression, idExpression.getParent(), context);
			}
		}
		
		return transformer;
	}
	
	private Transformer createComparisonTransformer(IASTIdExpression idExpression, IASTNode node, Context context) {
		Transformer transformer = null;
		Sentinel inFunctionSentinel = mapping.getInFunction().getSentinel();
		
		if(inFunctionSentinel == Sentinel.NULL) {
			if(ASTAnalyzer.isCheckedIfEqualToNull(node)) {
				transformer = new ComparisonTransformer(context, idExpression, mapping, true);
			}
			else if(ASTAnalyzer.isCheckedIfNotEqualToNull(node)) {
				transformer = new ComparisonTransformer(context, idExpression, mapping, false);
			}
		}
		else if(inFunctionSentinel == Sentinel.STRLEN) {
			if(ASTAnalyzer.isNodeComparedToStrlen(node, true)) {
				transformer = new ComparisonTransformer(context, idExpression, mapping, true);
			}
			else if(ASTAnalyzer.isNodeComparedToStrlen(node, false)) {
				transformer = new ComparisonTransformer(context, idExpression, mapping, false);
			}
		}
		
		return transformer;
	}
}
