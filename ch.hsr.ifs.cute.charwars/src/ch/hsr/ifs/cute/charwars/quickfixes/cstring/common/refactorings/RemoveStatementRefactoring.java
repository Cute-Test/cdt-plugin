package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class RemoveStatementRefactoring extends Refactoring {
	private Function inFunction;
	
	public RemoveStatementRefactoring(Function inFunction, ContextState... contextStates) {
		this.inFunction = inFunction;
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		if(ASTAnalyzer.isFunctionCallArg(idExpression, 0, inFunction)) {
			isApplicable = true;
		}
	}

	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setRemoveStatement(true);
	}
}
