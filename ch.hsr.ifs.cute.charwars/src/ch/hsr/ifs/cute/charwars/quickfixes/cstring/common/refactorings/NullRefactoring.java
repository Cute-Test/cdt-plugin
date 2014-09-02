package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.NullTransformer;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers.Transformer;

public class NullRefactoring extends Refactoring {
	@Override
	public Transformer createTransformer(IASTIdExpression idExpression, Context context) {
		Transformer transformer = null;
		IASTNode parent = idExpression.getParent();
		if(ASTAnalyzer.isPlusAssignment(parent) ||
			ASTAnalyzer.isCallToMemberFunction(parent, StdString.COMPARE) ||
			ASTAnalyzer.isCallToMemberFunction(parent, StdString.FIND_FIRST_OF) ||
			ASTAnalyzer.isCallToMemberFunction(parent, StdString.APPEND) ||
			ASTAnalyzer.isCallToMemberFunction(parent, StdString.REPLACE) ||
			ASTAnalyzer.isCallToMemberFunction(parent, StdString.FIND_FIRST_NOT_OF) ||
			ASTAnalyzer.isCallToMemberFunction(parent, StdString.FIND) ||
			ASTAnalyzer.isArraySubscriptExpression(idExpression) || 
			ASTAnalyzer.isLValueInAssignment(idExpression) ||
			(ASTAnalyzer.isLeftShiftExpressionToStdCout(parent) && !context.isPotentiallyModifiedCharPointer(idExpression)) ||
			(isStdStringParameterDeclaration(idExpression, context) && !context.isPotentiallyModifiedCharPointer(idExpression))) {
			transformer = new NullTransformer();
		}
		return transformer;
	}
	
	private boolean isStdStringParameterDeclaration(IASTIdExpression idExpression, Context context) {
		IType parameterType = ASTAnalyzer.getParameterType(idExpression);
		if(parameterType == null) return false;
		else return ASTAnalyzer.isStdStringType(parameterType);
	}
}
