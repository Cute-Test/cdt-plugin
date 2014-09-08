package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.transformers;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context;

public class ExpressionTransformer extends Transformer {
	public enum Transformation {
		SIZE,
		EMPTY,
		NOT_EMPTY,
		DEREFERENCED,
		MODIFIED,
		ARRAY_SUBSCRIPTION
	}
	
	private Transformation transformation;
	
	public ExpressionTransformer(Context context, IASTIdExpression idExpression, IASTNode nodeToReplace, Transformation transformation) {
		super(context, idExpression, nodeToReplace);
		this.transformation = transformation;
	}
	
	@Override
	protected IASTNode getReplacementNode() {
		switch(transformation) {
		case SIZE:
			return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.SIZE);
		case EMPTY:
			if(context.isPotentiallyModifiedCharPointer(idExpression)) {
				IASTIdExpression subscript = context.createPosVariableIdExpression();
				IASTArraySubscriptExpression arraySubscription = ExtendedNodeFactory.newArraySubscriptExpression(idExpression, subscript);
				return ExtendedNodeFactory.newLogicalNotExpression(arraySubscription);
			}
			else {
				return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.EMPTY);
			}
		case NOT_EMPTY:
			if(context.isPotentiallyModifiedCharPointer(idExpression)) {
				IASTIdExpression subscript = context.createPosVariableIdExpression();
				return ExtendedNodeFactory.newArraySubscriptExpression(idExpression, subscript);
			}
			else {
				IASTExpression emptyCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.EMPTY);
				return ExtendedNodeFactory.newLogicalNotExpression(emptyCall);
			}
		case DEREFERENCED:
			IASTExpression subscript;
			if(context.isPotentiallyModifiedCharPointer(idExpression)) {
				subscript = context.createPosVariableIdExpression();
			}
			else {
				subscript = ExtendedNodeFactory.newIntegerLiteral(0);
			}
			
			if(ASTAnalyzer.isAdditionExpression(idExpression.getParent())) {
				IASTBinaryExpression addition = (IASTBinaryExpression)idExpression.getParent();
				IASTExpression otherOperand = (addition.getOperand1() == idExpression) ? addition.getOperand2() : addition.getOperand1();
				
				if(context.isPotentiallyModifiedCharPointer(idExpression)) {
					subscript = ExtendedNodeFactory.newPlusExpression(subscript, otherOperand);
				}
				else {
					subscript = otherOperand;
				}
			}
			
			return ExtendedNodeFactory.newArraySubscriptExpression(idExpression, subscript);
		case MODIFIED:
			return context.createPosVariableIdExpression();
			
		case ARRAY_SUBSCRIPTION:
			IASTArraySubscriptExpression oldArraySubscriptExpression = (IASTArraySubscriptExpression)idExpression.getParent();
			IASTExpression oldArraySubscript = (IASTExpression)oldArraySubscriptExpression.getArgument();
			IASTExpression newArraySubscript = context.createPosVariableIdExpression();
			
			if(!ASTAnalyzer.isIntegerLiteral(oldArraySubscript, 0)) {
				newArraySubscript = ExtendedNodeFactory.newPlusExpression(newArraySubscript, oldArraySubscript);
			}
			return ExtendedNodeFactory.newArraySubscriptExpression(idExpression, newArraySubscript);
		default:
			return null;
		}
	}
}
