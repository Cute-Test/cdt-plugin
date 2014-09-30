package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.BoolAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.LiteralAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.UEAnalyzer;

public class ExpressionRefactoring extends Refactoring {
	private enum Transformation {
		SIZE,
		EMPTY,
		NOT_EMPTY,
		DEREFERENCED,
		MODIFIED,
		ARRAY_SUBSCRIPTION,
		INDEX_CALCULATION
	}
	
	private static final String TRANSFORMATION = "TRANSFORMATION";
	
	public ExpressionRefactoring(ContextState... contextStates) {
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		if(ASTAnalyzer.isStringLengthCalculation(idExpression)) {
			//sizeof(str) / sizeof(*str) - 1 -> str.size()
			//sizeof str / sizeof *str -1 -> str.size()
			IASTNode nodeToReplace = idExpression.getParent();
			while(!BEAnalyzer.isSubtraction(nodeToReplace)) {
				nodeToReplace = nodeToReplace.getParent();
			}
			isApplicable = true;
			config.put(NODE_TO_REPLACE, nodeToReplace);
			config.put(TRANSFORMATION, Transformation.SIZE);
		}
		else if(CheckAnalyzer.isCheckedForEmptiness(idExpression, true)) {
			//!*str -> str.empty()
			//*str == 0 -> str.empty()
			//if modified: !*str -> !str[str_pos]
			isApplicable = true;
			config.put(NODE_TO_REPLACE, BoolAnalyzer.getEnclosingBoolean(idExpression));
			config.put(TRANSFORMATION, Transformation.EMPTY);
		}
		else if(CheckAnalyzer.isCheckedForEmptiness(idExpression, false)) {
			//if(*str) -> if(!str.empty())
			//*str != 0 -> !str.empty()
			//if modified: *str -> str[str_pos]
			isApplicable = true;
			config.put(NODE_TO_REPLACE, BoolAnalyzer.getEnclosingBoolean(idExpression));
			config.put(TRANSFORMATION, Transformation.NOT_EMPTY);
		}
		else if(ASTAnalyzer.isDereferencedToChar(idExpression)) {
			//*str -> str[0]
			//*(str) -> str[0]
			//*(str+n) -> str[n]
			//if modified: *str -> str[str_pos]
			IASTNode nodeToReplace = idExpression.getParent();
			while(!UEAnalyzer.isDereferenceExpression(nodeToReplace)) {
				nodeToReplace = nodeToReplace.getParent();
			}
			isApplicable = true;
			config.put(NODE_TO_REPLACE, nodeToReplace);
			config.put(TRANSFORMATION, Transformation.DEREFERENCED);
		}
		else if(ASTAnalyzer.modifiesCharPointer(idExpression)) {
			//++str -> ++str_pos
			//*++str -> str[++str_pos]
			//str++ -> str_pos++
			//*str++ -> str[str_pos++]
			//str += n -> str_pos += n
			isApplicable = true;
			config.put(NODE_TO_REPLACE, idExpression);
			config.put(TRANSFORMATION, Transformation.MODIFIED);
		}
		else if(ASTAnalyzer.isArraySubscriptExpression(idExpression) && context.isOffset(idExpression)) {
			//str[0] -> str[str_pos]
			//str[1] -> str[str_pos + 1]
			isApplicable = true;
			config.put(NODE_TO_REPLACE, idExpression.getParent());
			config.put(TRANSFORMATION, Transformation.ARRAY_SUBSCRIPTION);
		}
		else if(context.getContextState() == ContextState.CStringAlias && ASTAnalyzer.isIndexCalculation(idExpression)) {
			//ptr - str -> ptr
			IASTNode nodeToReplace = idExpression.getParent();
			if(UEAnalyzer.isBracketExpression(nodeToReplace.getParent())) {
				nodeToReplace = nodeToReplace.getParent();
			}
			isApplicable = true;
			config.put(NODE_TO_REPLACE, nodeToReplace);
			config.put(TRANSFORMATION, Transformation.INDEX_CALCULATION);
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTName stringName = ExtendedNodeFactory.newName(context.getStringVarName());
		switch((Transformation)config.get(TRANSFORMATION)) {
		case SIZE:
			return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.SIZE);
		case EMPTY:
			if(context.isOffset(idExpression)) {
				IASTIdExpression subscript = context.createOffsetVarIdExpression();
				IASTArraySubscriptExpression arraySubscription = ExtendedNodeFactory.newArraySubscriptExpression(ExtendedNodeFactory.newIdExpression(stringName.toString()), subscript);
				return ExtendedNodeFactory.newLogicalNotExpression(arraySubscription);
			}
			else {
				return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.EMPTY);
			}
		case NOT_EMPTY:
			if(context.isOffset(idExpression)) {
				IASTIdExpression subscript = context.createOffsetVarIdExpression();
				return ExtendedNodeFactory.newArraySubscriptExpression(ExtendedNodeFactory.newIdExpression(stringName.toString()), subscript);
			}
			else {
				IASTExpression emptyCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.EMPTY);
				return ExtendedNodeFactory.newLogicalNotExpression(emptyCall);
			}
		case DEREFERENCED:
			IASTExpression subscript;
			if(context.isOffset(idExpression)) {
				subscript = context.createOffsetVarIdExpression();
			}
			else {
				subscript = ExtendedNodeFactory.newIntegerLiteral(0);
			}
			
			if(BEAnalyzer.isAddition(idExpression.getParent())) {
				IASTExpression otherOperand = BEAnalyzer.getOtherOperand(idExpression);
				
				if(context.isOffset(idExpression)) {
					subscript = ExtendedNodeFactory.newPlusExpression(subscript, otherOperand);
				}
				else {
					subscript = otherOperand;
				}
			}
			
			return ExtendedNodeFactory.newArraySubscriptExpression(idExpression, subscript);
		case MODIFIED:
			IASTNode parent = idExpression.getParent();
			if(UEAnalyzer.isIncrementation(parent)) {
				if(UEAnalyzer.isDereferenceExpression(parent.getParent())) {
					config.put(NODE_TO_REPLACE, parent.getParent());
					int operator = ((IASTUnaryExpression)parent).getOperator();
					IASTExpression arrExpr = ExtendedNodeFactory.newIdExpression(context.getStringVarName());
					IASTExpression subscriptExpr = ExtendedNodeFactory.newUnaryExpression(operator, context.createOffsetVarIdExpression());
					return ExtendedNodeFactory.newArraySubscriptExpression(arrExpr, subscriptExpr);
				}
			}
			return context.createOffsetVarIdExpression();
		case ARRAY_SUBSCRIPTION:
			IASTArraySubscriptExpression oldArraySubscriptExpression = (IASTArraySubscriptExpression)idExpression.getParent();
			IASTExpression oldArraySubscript = (IASTExpression)oldArraySubscriptExpression.getArgument();
			IASTExpression newArraySubscript = context.createOffsetVarIdExpression();
			
			if(!LiteralAnalyzer.isZero(oldArraySubscript)) {
				newArraySubscript = ExtendedNodeFactory.newPlusExpression(newArraySubscript, oldArraySubscript);
			}
			return ExtendedNodeFactory.newArraySubscriptExpression(idExpression, newArraySubscript);
		case INDEX_CALCULATION:
			return context.createOffsetVarIdExpression();
		default:
			return null;
		}
	}
}
