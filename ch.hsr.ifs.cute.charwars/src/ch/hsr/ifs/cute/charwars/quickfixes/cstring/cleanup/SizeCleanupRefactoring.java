package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.constants.StringType;

public class SizeCleanupRefactoring extends CleanupRefactoring {	
	public SizeCleanupRefactoring(IASTFunctionCallExpression functionCall, IASTFunctionCallExpression searchCall, ASTRewrite rewrite) {
		super(functionCall, searchCall, rewrite);
	}
	
	@Override
	public void performOptimized() {
		String resultVarName = getResultVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, resultVarName, searchCall);
		ASTModifier.replace(oldStatement, posVarDS, rewrite);
		
		IASTNode block = ASTAnalyzer.getEnclosingBlock(oldStatement);
		IASTName varName = ((IASTDeclarator)functionCall.getParent().getParent()).getName();
		
		SizeReturnValueVisitor visitor = new SizeReturnValueVisitor(varName, rewrite);
		block.accept(visitor);
	}
	
	@Override
	public void performNormal() {
		String resultVarName = getResultVarName(functionCall);
		IASTStatement oldStatementCopy = oldStatement.copy();
		IASTFunctionCallExpression functionCallCopy = getFunctionCallNode(oldStatementCopy);
		ASTModifier.replaceNode(functionCallCopy, searchCall);
		ASTModifier.insertBefore(oldStatement.getParent(), oldStatement, oldStatementCopy, rewrite);
		
		IASTIdExpression resultVarIdExpression = ExtendedNodeFactory.newIdExpression(resultVarName);
		IASTBinaryExpression condition = ExtendedNodeFactory.newEqualityComparison(resultVarIdExpression, ExtendedNodeFactory.newNposExpression(StringType.STRING), true);
		IASTBinaryExpression assignment = ExtendedNodeFactory.newAssignment(ExtendedNodeFactory.newIdExpression(resultVarName), ExtendedNodeFactory.newMemberFunctionCallExpression(str.getName(), StdString.SIZE));
		IASTStatement assignmentStatement = ExtendedNodeFactory.newExpressionStatement(assignment);
		IASTCompoundStatement ifBody = ExtendedNodeFactory.newCompoundStatement(assignmentStatement);
		IASTIfStatement ifStatement = ExtendedNodeFactory.newIfStatement(condition, ifBody);
		
		ASTModifier.insertBefore(oldStatement.getParent(), oldStatement, ifStatement, rewrite);
		ASTModifier.remove(oldStatement, rewrite);
	}
}
