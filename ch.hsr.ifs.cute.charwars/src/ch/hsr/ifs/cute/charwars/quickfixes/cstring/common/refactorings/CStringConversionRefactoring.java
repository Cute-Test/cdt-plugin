package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.CStringConversionTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class CStringConversionRefactoring extends Refactoring {
	private Mapping mapping;
	
	public CStringConversionRefactoring(Mapping mapping) {
		this.mapping = mapping;
	}
	
	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		
		if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, mapping.getInFunction().getName())) {
			IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)idExpression.getParent(); 
			if(ASTAnalyzer.isAssignedToCharPointer(functionCall, false)) {
				transformer = new CStringConversionTransformer(context, idExpression, false);
			}
			else if(ASTAnalyzer.isAssignedToCharPointer(functionCall, true)) {
				transformer = new CStringConversionTransformer(context, idExpression, true);
			}
		}
		return transformer;
	}
}
