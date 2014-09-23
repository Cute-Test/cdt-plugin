package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.TypeAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.ASTChangeDescription;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class NullRefactoring extends Refactoring {
	public NullRefactoring(ContextState... contextStates) {
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		IASTNode parent = idExpression.getParent();
		boolean isNotOffset = !context.isOffset(idExpression);
		
		if(ASTAnalyzer.isPlusAssignment(parent) ||
			ASTAnalyzer.isCallToMemberFunction(parent, Function.COMPARE) ||
			ASTAnalyzer.isCallToMemberFunction(parent, Function.FIND_FIRST_OF) ||
			ASTAnalyzer.isCallToMemberFunction(parent, Function.APPEND) ||
			ASTAnalyzer.isCallToMemberFunction(parent, Function.REPLACE) ||
			ASTAnalyzer.isCallToMemberFunction(parent, Function.FIND_FIRST_NOT_OF) ||
			ASTAnalyzer.isCallToMemberFunction(parent, Function.FIND) ||
			(ASTAnalyzer.isArraySubscriptExpression(idExpression) && isNotOffset) || 
			ASTAnalyzer.isLValueInAssignment(idExpression) ||
			(ASTAnalyzer.isLeftShiftExpressionToStdCout(parent) && isNotOffset) ||
			(isStdStringParameterDeclaration(idExpression, context) && isNotOffset)) {
			
			isApplicable = true;
		}
	}

	private boolean isStdStringParameterDeclaration(IASTIdExpression idExpression, Context context) {
		IType parameterType = ASTAnalyzer.getParameterType(idExpression);
		if(parameterType == null) return false;
		else return TypeAnalyzer.isStdStringType(parameterType);
	}

	@Override
	protected void updateChangeDescription(ASTChangeDescription changeDescription) {
		//do nothing
	}
}
