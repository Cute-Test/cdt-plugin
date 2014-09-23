package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IType;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.asttools.TypeAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;

public class DefaultRefactoring extends Refactoring {
	private static final String CONVERT_TO_CONST = "CONVERT_TO_CONST";
	
	public DefaultRefactoring(ContextState... contextStates) {
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		IType parameterType = ASTAnalyzer.getParameterType(idExpression);
		
		isApplicable = true;
		config.put(NODE_TO_REPLACE, idExpression);
		
		if(parameterType != null && TypeAnalyzer.isCStringType(parameterType, false)) {
			config.put(CONVERT_TO_CONST, false);
		}
		else {
			config.put(CONVERT_TO_CONST, true);
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTName stringName = ExtendedNodeFactory.newName(context.getStringVarName());
		boolean convertToConst = (boolean)config.get(CONVERT_TO_CONST);
		
		if(convertToConst) {
			IASTFunctionCallExpression c_strCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.C_STR);
			if(context.isOffset(idExpression)) {
				IASTBinaryExpression plusExpression = ExtendedNodeFactory.newPlusExpression(c_strCall, context.createOffsetVarIdExpression());
				return ExtendedNodeFactory.newBracketedExpression(plusExpression);
			}
			else {
				return c_strCall;
			}	
		}
		else {
			IASTFunctionCallExpression beginCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);			
			IASTUnaryExpression dereferenceOperatorExpression = ExtendedNodeFactory.newDereferenceOperatorExpression(beginCall);
			return ExtendedNodeFactory.newAdressOperatorExpression(dereferenceOperatorExpression); 
		}
	}
}
