package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.CheckAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.Context.ContextState;
import ch.hsr.ifs.cute.charwars.utils.BoolAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;

public class OperatorRefactoring extends Refactoring {
	private Function inFunction;
	private Function outFunction;
	
	public OperatorRefactoring(Function inFunction, Function outFunction, ContextState... contextStates) {
		this.inFunction = inFunction;
		this.outFunction = outFunction;
		setContextStates(contextStates);
	}
	
	@Override
	protected void prepareConfiguration(IASTIdExpression idExpression, Context context) {
		if(!context.isOffset(idExpression)) {
			boolean isStrcmpOrWcscmp = (inFunction == Function.STRCMP) || (inFunction == Function.WCSCMP);
			
			if(isStrcmpOrWcscmp) {
				boolean isStringEqualityCheck = outFunction == Function.OP_EQUALS && CheckAnalyzer.isPartOfStringCheck(idExpression, true); 
				boolean isStringInequalityCheck = outFunction == Function.OP_NOT_EQUALS && CheckAnalyzer.isPartOfStringCheck(idExpression, false);
				
				if(isStringEqualityCheck || isStringInequalityCheck) {
					isApplicable = true;
					config.put(NODE_TO_REPLACE, BoolAnalyzer.getEnclosingBoolean(idExpression));
				}
			}
			else {
				boolean isOther = FunctionAnalyzer.isFunctionCallArg(idExpression, 0, inFunction);
				
				if(isOther) {
					isApplicable = true;
					config.put(NODE_TO_REPLACE, idExpression.getParent());
				}
			}

		}
	}

	@Override
	protected IASTNode getReplacementNode(IASTIdExpression idExpression, Context context) {
		IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)idExpression.getParent();
		IASTInitializerClause[] args = functionCall.getArguments();
		IASTExpression lhs = idExpression;
		
		if(outFunction == Function.OP_ASSIGNMENT) {
			IASTExpression rhs = (IASTExpression)ASTAnalyzer.extractStdStringArg(args[1]);
			return ExtendedNodeFactory.newAssignment(lhs, rhs);
		}
		else if(outFunction == Function.OP_PLUS_ASSIGNMENT) {
			IASTExpression rhs = (IASTExpression)ASTAnalyzer.extractStdStringArg(args[1]);
			return ExtendedNodeFactory.newPlusAssignment(lhs, rhs);
		}
		else if(outFunction == Function.OP_EQUALS || outFunction == Function.OP_NOT_EQUALS) {
			IASTExpression rhs = (IASTExpression)(args[0] == idExpression ? args[1] : args[0]);
			return ExtendedNodeFactory.newEqualityComparison(lhs, rhs, outFunction == Function.OP_EQUALS);
		}
		return null;
	}
}
