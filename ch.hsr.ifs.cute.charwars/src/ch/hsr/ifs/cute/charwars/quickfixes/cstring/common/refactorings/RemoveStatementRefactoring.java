package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class RemoveStatementRefactoring extends Refactoring {
	private Function inFunction;
	
	public RemoveStatementRefactoring(Function inFunction, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.contextStates = new HashSet<ContextState>();
		for(ContextState contextState : contextStates) {
			this.contextStates.add(contextState);
		}
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		if(ASTAnalyzer.isFunctionCallArgument(idExpression, 0, inFunction.getName())) {
			isApplicable = true;
		}
	}

	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		changeDescription.setRemoveStatement(true);
	}
}
