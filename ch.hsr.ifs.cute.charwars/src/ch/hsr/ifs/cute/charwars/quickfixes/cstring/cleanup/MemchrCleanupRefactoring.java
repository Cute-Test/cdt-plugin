package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;

public class MemchrCleanupRefactoring extends CleanupRefactoring {
	public MemchrCleanupRefactoring(IASTFunctionCallExpression functionCall, Function inFunction, Function outFunction, ASTRewrite rewrite) {
		super(functionCall, inFunction, outFunction, rewrite);
	}
	
	@Override
	protected boolean optimizedRefactoringPossible() {
		return false;
	}
	
	@Override
	protected void performNormal() {
		String posVarName = getPosVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(Constants.AUTO, posVarName, newOutFunctionCall());
		IASTExpression conditionalExpression = newMemchrConditionalExpression(posVarName);
		if(functionCall.getParent() instanceof IASTCastExpression) {
			conditionalExpression = ExtendedNodeFactory.newBracketedExpression(conditionalExpression);
		}
	
		if(oldStatement.getParent() instanceof IASTIfStatement) {
			IASTStatement oldStatementCopy = oldStatement.copy();
			IASTFunctionCallExpression functionCallCopy = getFunctionCallNode(oldStatementCopy);
			ASTModifier.replaceNode(functionCallCopy, conditionalExpression);
			IASTCompoundStatement compoundStatement = ExtendedNodeFactory.newCompoundStatement(posVarDS, oldStatementCopy);
			ASTModifier.replace(oldStatement, compoundStatement, rewrite);
		}
		else {
			ASTModifier.insertBefore(oldStatement.getParent(), oldStatement, posVarDS, rewrite);
			ASTModifier.replace(functionCall, conditionalExpression, rewrite);
		}
	}

	@Override
	protected void performOptimized() { 
		//not available for MemchrCleanupRefactoring 
	}
	
	private IASTFunctionCallExpression newOutFunctionCall() {
		IASTExpression first = newStartIterator();
		IASTExpression last = newEndIterator();
		return ExtendedNodeFactory.newFunctionCallExpression(outFunction.getName(), first, last, secondArg.copy());
	}
	
	private IASTExpression newStartIterator() {
		IASTName stringName = str.getName();
		return ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);
	}
	
	private IASTExpression newEndIterator() {
		IASTName stringName = str.getName();
		IASTNode thirdArg = functionCall.getArguments()[2];
		IASTExpression last = null;
		
		if(FunctionAnalyzer.isCallToMemberFunction(thirdArg, Function.SIZE)) {
			IASTFunctionCallExpression sizeCall = (IASTFunctionCallExpression)thirdArg;
			IASTFieldReference functionNameExpr = (IASTFieldReference)sizeCall.getFunctionNameExpression();
			IASTIdExpression fieldOwner = (IASTIdExpression)functionNameExpr.getFieldOwner();
			if(ASTAnalyzer.isSameName(fieldOwner.getName(), stringName)) {
				last = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.END);
			}
		}
		
		if(last == null) {
			IASTExpression lhs = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);
			last = ExtendedNodeFactory.newPlusExpression(lhs.copy(), (IASTExpression)thirdArg.copy());
		}
		return last;
	} 
	
	private IASTConditionalExpression newMemchrConditionalExpression(String posVarName) {
		IASTIdExpression posVar = ExtendedNodeFactory.newIdExpression(posVarName);
		IASTExpression condition = ExtendedNodeFactory.newEqualityComparison(posVar, newEndIterator(), false);
		IASTExpression positive = ExtendedNodeFactory.newAdressOperatorExpression(ExtendedNodeFactory.newDereferenceOperatorExpression(posVar));
		IASTExpression negative = ExtendedNodeFactory.newIdExpression(Constants.NULLPTR);
		return ExtendedNodeFactory.newConditionalExpression(condition, positive, negative);
	}
}