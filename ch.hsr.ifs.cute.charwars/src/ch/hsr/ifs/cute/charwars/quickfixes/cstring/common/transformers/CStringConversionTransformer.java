package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;

public class CStringConversionTransformer extends Transformer {
	private boolean convertToConst;
	
	public CStringConversionTransformer(Context context, IASTIdExpression idExpression, boolean convertToConst) {
		super(context, idExpression,  idExpression);
		this.convertToConst = convertToConst;
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		if(convertToConst) {
			IASTFunctionCallExpression c_strCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.C_STR);
			if(context.isPotentiallyModifiedCharPointer(idExpression)) {
				IASTBinaryExpression plusExpression = ExtendedNodeFactory.newPlusExpression(c_strCall, ExtendedNodeFactory.newIdExpression(context.getPosVariableName()));
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
