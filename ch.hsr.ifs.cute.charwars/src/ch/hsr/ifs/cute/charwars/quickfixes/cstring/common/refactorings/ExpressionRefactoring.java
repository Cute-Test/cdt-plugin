package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.BEAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.BoolAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
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
		INDEX_CALCULATION,
		ALIAS_COMPARISON
	}
	
	private static final String TRANSFORMATION = "TRANSFORMATION";
	
	public ExpressionRefactoring(ContextState... contextStates) {
		setContextStates(contextStates);
	}
	
	private void makeApplicable(IASTNode nodeToReplace, Transformation transformation) {
		super.makeApplicable(nodeToReplace);
		config.put(TRANSFORMATION, transformation);
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
			makeApplicable(nodeToReplace, Transformation.SIZE);
		}
		else if(CheckAnalyzer.isCheckedForEmptiness(idExpression, true)) {
			//!*str -> str.empty()
			//*str == 0 -> str.empty()
			//if modified: !*str -> !str[str_pos]
			makeApplicable(BoolAnalyzer.getEnclosingBoolean(idExpression), Transformation.EMPTY);
		}
		else if(CheckAnalyzer.isCheckedForEmptiness(idExpression, false)) {
			//if(*str) -> if(!str.empty())
			//*str != 0 -> !str.empty()
			//if modified: *str -> str[str_pos]
			makeApplicable(BoolAnalyzer.getEnclosingBoolean(idExpression), Transformation.NOT_EMPTY);
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
			makeApplicable(nodeToReplace, Transformation.DEREFERENCED);
		}
		else if(ASTAnalyzer.modifiesCharPointer(idExpression)) {
			//++str -> ++str_pos
			//*++str -> str[++str_pos]
			//str++ -> str_pos++
			//*str++ -> str[str_pos++]
			//str += n -> str_pos += n
			makeApplicable(idExpression, Transformation.MODIFIED);
		}
		else if(ASTAnalyzer.isArraySubscriptExpression(idExpression) && context.isOffset(idExpression)) {
			//str[0] -> str[str_pos]
			//str[1] -> str[str_pos + 1]
			makeApplicable(idExpression.getParent(), Transformation.ARRAY_SUBSCRIPTION);
		}
		else if(context.getContextState() == ContextState.CStringAlias && ASTAnalyzer.isIndexCalculation(idExpression)) {
			//ptr - str -> ptr
			IASTNode nodeToReplace = idExpression.getParent();
			if(UEAnalyzer.isBracketExpression(nodeToReplace.getParent())) {
				nodeToReplace = nodeToReplace.getParent();
			}
			makeApplicable(nodeToReplace, Transformation.INDEX_CALCULATION);
		}
		else if(context.getContextState() == ContextState.CStringAlias && !ASTAnalyzer.isLValueInAssignment(idExpression) && (CheckAnalyzer.isNodeComparedToNull(idExpression) || CheckAnalyzer.isNodeComparedToStrlen(idExpression))) {
			makeApplicable(BoolAnalyzer.getEnclosingBoolean(idExpression), Transformation.ALIAS_COMPARISON);
		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTName stringVarName = context.createStringVarName();
		switch((Transformation)config.get(TRANSFORMATION)) {
		case SIZE:
			return ExtendedNodeFactory.newMemberFunctionCallExpression(stringVarName, StdString.SIZE);
		case EMPTY:
			if(context.isOffset(idExpression)) {
				IASTIdExpression subscript = context.createOffsetVarIdExpression();
				IASTArraySubscriptExpression arraySubscription = ExtendedNodeFactory.newArraySubscriptExpression(context.createStringVarIdExpression(), subscript);
				return ExtendedNodeFactory.newLogicalNotExpression(arraySubscription);
			}
			else {
				return ExtendedNodeFactory.newMemberFunctionCallExpression(stringVarName, StdString.EMPTY);
			}
		case NOT_EMPTY:
			if(context.isOffset(idExpression)) {
				IASTIdExpression subscript = context.createOffsetVarIdExpression();
				return ExtendedNodeFactory.newArraySubscriptExpression(context.createStringVarIdExpression(), subscript);
			}
			else {
				IASTExpression emptyCall = ExtendedNodeFactory.newMemberFunctionCallExpression(stringVarName, StdString.EMPTY);
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
			return ExtendedNodeFactory.newArraySubscriptExpression(context.createStringVarIdExpression(), subscript);
		case MODIFIED:
			IASTNode parent = idExpression.getParent();
			if(UEAnalyzer.isIncrementation(parent)) {
				if(UEAnalyzer.isDereferenceExpression(parent.getParent())) {
					config.put(NODE_TO_REPLACE, parent.getParent());
					int operator = ((IASTUnaryExpression)parent).getOperator();
					IASTExpression subscriptExpr = ExtendedNodeFactory.newUnaryExpression(operator, context.createOffsetVarIdExpression());
					return ExtendedNodeFactory.newArraySubscriptExpression(context.createStringVarIdExpression(), subscriptExpr);
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
			return ExtendedNodeFactory.newArraySubscriptExpression(context.createStringVarIdExpression(), newArraySubscript);
		case INDEX_CALCULATION:
			return context.createOffsetVarIdExpression();
		case ALIAS_COMPARISON:
			IASTExpression lhs = context.createOffsetVarIdExpression();
			IASTExpression rhs = ExtendedNodeFactory.newNposExpression(context.getStringType());
			boolean isEqual;
			if(CheckAnalyzer.isNodeComparedToNull(idExpression)) {
				isEqual = CheckAnalyzer.isNodeComparedToNull(idExpression, true);
			}
			else {
				isEqual = CheckAnalyzer.isNodeComparedToStrlen(idExpression, true);
			}
			return ExtendedNodeFactory.newEqualityComparison(lhs, rhs, isEqual);
		default:
			return null;
		}
	}
}
