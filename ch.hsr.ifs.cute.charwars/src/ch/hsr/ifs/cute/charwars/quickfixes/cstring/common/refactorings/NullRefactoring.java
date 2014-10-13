package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.FunctionBindingAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.analyzers.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.TypeAnalyzer;

public class NullRefactoring extends Refactoring {
	public NullRefactoring(ContextState... contextStates) {
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		IASTNode parent = idExpression.getParent();
		boolean isNotOffset = !context.isOffset(idExpression);
		
		if(BEAnalyzer.isPlusAssignment(parent) ||
			FunctionAnalyzer.isCallToMemberFunction(parent, Function.COMPARE) ||
			FunctionAnalyzer.isCallToMemberFunction(parent, Function.FIND_FIRST_OF) ||
			FunctionAnalyzer.isCallToMemberFunction(parent, Function.APPEND) ||
			FunctionAnalyzer.isCallToMemberFunction(parent, Function.REPLACE) ||
			FunctionAnalyzer.isCallToMemberFunction(parent, Function.FIND_FIRST_NOT_OF) ||
			FunctionAnalyzer.isCallToMemberFunction(parent, Function.FIND) ||
			(ASTAnalyzer.isArraySubscriptExpression(idExpression) && isNotOffset) || 
			ASTAnalyzer.isLValueInAssignment(idExpression) ||
			(ASTAnalyzer.isLeftShiftExpressionToStdCout(parent) && isNotOffset) ||
			(isStdStringParameterDeclaration(idExpression, context) && isNotOffset)) {
			
			makeApplicable(null);
		}
	}

	private boolean isStdStringParameterDeclaration(IASTIdExpression idExpression, Context context) {
		IType parameterType = FunctionBindingAnalyzer.getParameterType(idExpression);
		if(parameterType == null) return false;
		else return TypeAnalyzer.isStdStringType(parameterType);
	}

	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		//do nothing
	}
}
