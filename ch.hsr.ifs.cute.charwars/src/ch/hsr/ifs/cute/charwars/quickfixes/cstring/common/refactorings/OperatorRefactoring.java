package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.OperatorTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class OperatorRefactoring extends Refactoring {
	private Mapping mapping;
	
	public OperatorRefactoring(Mapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		String inFunctionName = mapping.getInFunction().getName();
		String outFunctionName = mapping.getOutFunction().getName();
		
		boolean isOther = !inFunctionName.equals(CString.STRCMP) && ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunctionName);
		boolean isStringEqualityCheck = inFunctionName.equals(CString.STRCMP) && outFunctionName.equals(StdString.OP_EQUALS) && ASTAnalyzer.isPartOfStringEqualityCheck(idExpression); 
		boolean isStringInequalityCheck = inFunctionName.equals(CString.STRCMP) && outFunctionName.equals(StdString.OP_NOT_EQUALS) && ASTAnalyzer.isPartOfStringInequalityCheck(idExpression);
		
		if(!context.isPotentiallyModifiedCharPointer(idExpression) && (isOther || isStringEqualityCheck || isStringInequalityCheck)) {
			transformer = new OperatorTransformer(context, idExpression, mapping);
		}

		return transformer;
	}

}
