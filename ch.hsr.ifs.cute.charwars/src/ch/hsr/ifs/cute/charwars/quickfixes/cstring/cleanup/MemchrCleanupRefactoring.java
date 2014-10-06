package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Algorithm;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;

public class MemchrCleanupRefactoring extends CleanupRefactoring {
	public MemchrCleanupRefactoring(IASTFunctionCallExpression functionCall, IASTFunctionCallExpression searchCall, ASTRewrite rewrite) {
		super(functionCall, searchCall, rewrite);
	}
	
	@Override
	public void performNormal() {
		String posVarName = getPosVarName(functionCall);
		IASTName stringName = str.getName();
		IASTNode secondArg = ASTAnalyzer.extractStdStringArg(functionCall.getArguments()[1]);
		IASTNode thirdArg = functionCall.getArguments()[2];
		IASTExpression last = null;
		
		if(FunctionAnalyzer.isCallToMemberFunction(thirdArg, Function.SIZE)) {
			if(thirdArg.getChildren()[0] instanceof ICPPASTExpression) {
				IASTIdExpression thirdArgIdExpression = (IASTIdExpression)thirdArg.getChildren()[0].getChildren()[0];
				if(ASTAnalyzer.isSameName(thirdArgIdExpression.getName(), str.getName())) {
					last = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.END);
				}
			}
		}
		
		if(last == null) {
			IASTExpression lhs = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);
			last = ExtendedNodeFactory.newPlusExpression(lhs.copy(), (IASTExpression)thirdArg.copy());
		}
		IASTExpression first = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);
		IASTFunctionCallExpression searchCall = ExtendedNodeFactory.newFunctionCallExpression(Algorithm.FIND, first, last, secondArg.copy());
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(Constants.AUTO, posVarName, searchCall);
		IASTExpression conditionalExpression = newMemchrConditionalExpression(posVarName, str, last);
		
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
	public void performOptimized() { 
		//not available for MemchrCleanupRefactoring 
	}
	
	private IASTConditionalExpression newMemchrConditionalExpression(String posVarName, IASTIdExpression strNode, IASTExpression last) {
		IASTIdExpression posVar = ExtendedNodeFactory.newIdExpression(posVarName);
		IASTExpression condition = ExtendedNodeFactory.newEqualityComparison(posVar, last, false);
		IASTExpression positive = ExtendedNodeFactory.newAdressOperatorExpression(ExtendedNodeFactory.newDereferenceOperatorExpression(posVar));
		IASTExpression negative = ExtendedNodeFactory.newIdExpression(Constants.NULLPTR);
		return ExtendedNodeFactory.newConditionalExpression(condition, positive, negative);
	}
}
