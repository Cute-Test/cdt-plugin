package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.CStringConversionTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class DefaultRefactoring extends Refactoring {
	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		IType parameterType = ASTAnalyzer.getParameterType(idExpression);
		
		if(parameterType != null && ASTAnalyzer.isCStringType(parameterType)) {
			transformer = new CStringConversionTransformer(context, idExpression, false); 
		}
		else {
			transformer = new CStringConversionTransformer(context, idExpression, true);
		}
		return transformer;
	}
}
