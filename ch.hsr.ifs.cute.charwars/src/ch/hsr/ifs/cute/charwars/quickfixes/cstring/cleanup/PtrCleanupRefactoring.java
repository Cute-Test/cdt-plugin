package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
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
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoringConfiguration;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;

public class PtrCleanupRefactoring extends CleanupRefactoring {
	public PtrCleanupRefactoring(IASTFunctionCallExpression functionCall, Function inFunction, Function outFunction, ASTRewrite rewrite) {
		super(functionCall, inFunction, outFunction, rewrite);
	}
		
	@Override
	protected void performOptimized() {
		String posVarName = getPosVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, posVarName, newOutFunctionCall());
		IASTNode block = ASTAnalyzer.getEnclosingBlock(oldStatement);
		IASTName varName = ((IASTDeclarator)functionCall.getParent().getParent()).getName();
		
		BlockRefactoringConfiguration config = new BlockRefactoringConfiguration();
		config.setBlock(block);
		config.setASTRewrite(rewrite);
		config.setStringType(StringType.STRING);
		config.setStrName(str.getName());
		config.setVarName(varName);
		config.setNewVarNameString(posVarName);
		
		BlockRefactoring blockRefactoring = new BlockRefactoring(config);
		blockRefactoring.refactorAllStatements();
		
		ASTModifier.insertBefore(oldStatement.getParent(), oldStatement, posVarDS, rewrite);
		ASTModifier.remove(oldStatement, rewrite);
	}
	
	@Override
	protected void performNormal() {
		String posVarName = getPosVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, posVarName, newOutFunctionCall());
		IASTConditionalExpression conditionalExpression = newConditionalExpression(posVarName);
		
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
	
	private IASTConditionalExpression newConditionalExpression(String posVarName) {
		IASTIdExpression posVar = ExtendedNodeFactory.newIdExpression(posVarName);
		IASTExpression condition = ExtendedNodeFactory.newEqualityComparison(posVar, ExtendedNodeFactory.newNposExpression(StringType.STRING), false);
		IASTExpression positive = ExtendedNodeFactory.newAdressOperatorExpression(ExtendedNodeFactory.newArraySubscriptExpression(str.copy(), posVar));
		IASTExpression negative = ExtendedNodeFactory.newIdExpression(Constants.NULLPTR);
		return ExtendedNodeFactory.newConditionalExpression(condition, positive, negative);
	}
	
	private IASTFunctionCallExpression newOutFunctionCall() {
		String outFunctionName = outFunction.getName();
		return ExtendedNodeFactory.newMemberFunctionCallExpression(str.getName(), outFunctionName, secondArg.copy());
	}
}