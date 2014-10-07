package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;

public class RemoveStatementRefactoring extends Refactoring {
	private Function inFunction;
	
	public RemoveStatementRefactoring(Function inFunction, ContextState... contextStates) {
		this.inFunction = inFunction;
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		if(FunctionAnalyzer.isFunctionCallArg(idExpression, 0, inFunction)) {
			makeApplicable(null);
		}
	}

	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setRemoveStatement(true);
	}
}
