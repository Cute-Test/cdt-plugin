package ch.hsr.ifs.cute.charwars.quickfixes.cstring.general;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoring;

public class CStringQuickFix extends BaseQuickFix {
	@Override
	public String getLabel() {
		return QuickFixLabels.C_STRING;
	}
	
	@Override
	protected String getErrorMessage() {
		return ErrorMessages.C_STRING_QUICK_FIX;
	}
	
	@Override
	protected void handleMarkedNode(IASTNode markedNode, ASTRewriteCache rewriteCache) {
		IASTDeclarator oldDeclarator = (IASTDeclarator)markedNode;
		IASTNode block =  ASTAnalyzer.getEnclosingBlock(oldDeclarator);
		boolean isNotNested = (block == oldDeclarator.getTranslationUnit() || 
				block instanceof ICPPASTNamespaceDefinition ||
				block instanceof IASTCompositeTypeSpecifier );
		IASTSimpleDeclaration oldDeclaration = (IASTSimpleDeclaration)oldDeclarator.getParent();
		IASTDeclarationStatement oldDeclarationStatement = isNotNested ? null : (IASTDeclarationStatement)oldDeclaration.getParent();
		IASTNode beforeNode = isNotNested ? oldDeclaration : oldDeclarationStatement;
		ASTRewrite rewrite = rewriteCache.getASTRewrite(markedNode.getTranslationUnit().getOriginatingTranslationUnit());
		
		for(IASTDeclarator declarator : oldDeclaration.getDeclarators()) {
			insertNewDeclarationStatementFromDeclarator(declarator, beforeNode, declarator.equals(oldDeclarator), block, rewrite);
		}
		
		ASTModifier.remove(beforeNode, rewrite);
		
		IASTName oldName = oldDeclarator.getName();
		BlockRefactoring blockRefactoring = new BlockRefactoring(rewrite, oldName, block, oldDeclarationStatement);
		blockRefactoring.refactorAllStatements();
		
		headers.addAll(blockRefactoring.getHeadersToInclude());
		headers.add(StdString.HEADER_NAME);
	}
	
	private int getStringBufferSizeFromDeclarator(IASTDeclarator declarator) {
		int stringBufferSize = -1;
		for(IASTNode child : declarator.getChildren()) {
			if(child instanceof IASTArrayModifier && ((IASTArrayModifier) child).getConstantExpression() != null) {
				stringBufferSize = Integer.parseInt(((IASTArrayModifier) child).getConstantExpression().getRawSignature());
				break;
			}
		}
		return stringBufferSize;
	}
	
	private IASTDeclarationStatement newRefactoredDeclarationStatementFromDeclarator(IASTDeclarator declarator) {
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)declarator.getParent();
		
		IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declaration.getDeclSpecifier();
		IASTDeclSpecifier newDeclSpecifier = newRefactoredDeclSpecifier(simpleDeclSpecifier, declarator);
		IASTSimpleDeclaration newDeclaration = ExtendedNodeFactory.newSimpleDeclaration(newDeclSpecifier);
		IASTDeclarator newDeclarator = ExtendedNodeFactory.newDeclarator(declarator.getName().toString());
		
		IASTInitializer initializer;
		if(ASTAnalyzer.hasStrdupAssignment(declarator)) {
			IASTFunctionCallExpression strdupCall = (IASTFunctionCallExpression)ASTAnalyzer.getInitializerClause(declarator);
			initializer = ExtendedNodeFactory.newEqualsInitializer(strdupCall.getArguments()[0].copy());
		}
		else {
			initializer = declarator.getInitializer().copy();
		}
		
		newDeclarator.setInitializer(initializer);
		newDeclaration.addDeclarator(newDeclarator);
		return ExtendedNodeFactory.newDeclarationStatement(newDeclaration);
	}
	
	private IASTDeclSpecifier newRefactoredDeclSpecifier(IASTSimpleDeclSpecifier oldDeclSpecifier, IASTDeclarator oldDeclarator) {
		IASTDeclSpecifier newDeclSpecifier = ExtendedNodeFactory.newNamedTypeSpecifier(ASTAnalyzer.getStringReplacementType(oldDeclSpecifier));
		newDeclSpecifier.setStorageClass(oldDeclSpecifier.getStorageClass());
		if(oldDeclarator.getPointerOperators().length > 0) {
			IASTPointer pointer = (IASTPointer) oldDeclarator.getPointerOperators()[0];
			newDeclSpecifier.setConst(pointer.isConst() && oldDeclSpecifier.isConst());
		}
		newDeclSpecifier.setVolatile(oldDeclSpecifier.isVolatile());
		return newDeclSpecifier;
	}
	
	private void insertNewDeclarationStatementFromDeclarator(IASTDeclarator declarator, IASTNode beforeNode, boolean isOldDeclarator, IASTNode block, ASTRewrite rewrite) {
		boolean isGlobal = (block == declarator.getTranslationUnit());
		
		IASTNode nodeToInsert = isOldDeclarator ? newRefactoredDeclarationStatementFromDeclarator(declarator) : ExtendedNodeFactory.newDeclarationStatementFromDeclarator(declarator);
		if(isGlobal) {
			nodeToInsert = ((IASTDeclarationStatement)nodeToInsert).getDeclaration();
		}
		
		ASTModifier.insertBefore(block, beforeNode, nodeToInsert, rewrite);
			
		if(isOldDeclarator && !isGlobal) {
			int stringBufferSize = getStringBufferSizeFromDeclarator(declarator);
			if(stringBufferSize != -1) {
				IASTLiteralExpression new_cap = ExtendedNodeFactory.newIntegerLiteral(stringBufferSize);
				IASTFunctionCallExpression reserveCall = ExtendedNodeFactory.newMemberFunctionCallExpression(declarator.getName(), StdString.RESERVE, new_cap);
				IASTExpressionStatement reserveCallStatement = ExtendedNodeFactory.newExpressionStatement(reserveCall);
				ASTModifier.insertBefore(block, beforeNode, reserveCallStatement , rewrite);		
			}
		}
	}
}