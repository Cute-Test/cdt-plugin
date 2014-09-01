package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.Mapping;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.RemoveStatementTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class RemoveStatementRefactoring extends Refactoring {
	private Mapping mapping;
	
	public RemoveStatementRefactoring(Mapping mapping) {
		this.mapping = mapping;
	}
	
	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		
		if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, mapping.getInFunction().getName())) {
			transformer = new RemoveStatementTransformer();
		}

		return transformer;
	}
}
