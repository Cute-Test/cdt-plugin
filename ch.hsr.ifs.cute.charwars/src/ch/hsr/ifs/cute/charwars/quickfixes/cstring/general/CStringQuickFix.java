package ch.hsr.ifs.cute.charwars.quickfixes.cstring.general;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
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
import ch.hsr.ifs.cute.charwars.asttools.DeclaratorAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoring;
import ch.hsr.ifs.cute.charwars.utils.DeclaratorTypeAnalyzer;

public class CStringQuickFix extends BaseQuickFix {
	@Override
	public String getLabel() {
		String problemId = getProblemId(currentMarker);
		if(problemId.equals(ProblemIDs.C_STRING_PROBLEM)) {
			return QuickFixLabels.C_STRING;
		}
		else if(problemId.equals(ProblemIDs.C_STRING_ALIAS_PROBLEM)) {
			return QuickFixLabels.C_STRING_ALIAS;
		}
		return "Unknown problem.";
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
		
		IASTName variableToRefactor = oldDeclarator.getName();
		String stringName;
		boolean isAlias = (getProblemId(currentMarker).equals(ProblemIDs.C_STRING_ALIAS_PROBLEM));
		
		if(isAlias) {
			IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)oldDeclarator.getInitializer();
			IASTFunctionCallExpression cstrCall = (IASTFunctionCallExpression)equalsInitializer.getInitializerClause();
			IASTFieldReference fieldReference = (IASTFieldReference)cstrCall.getFunctionNameExpression();
			IASTIdExpression idExpression = (IASTIdExpression)fieldReference.getFieldOwner();
			stringName = idExpression.getName().toString();
		}
		else {
			stringName = variableToRefactor.toString();
		}
		
		StringType stringType = StringType.createFromDeclSpecifier(oldDeclaration.getDeclSpecifier());
		BlockRefactoring blockRefactoring = new BlockRefactoring(rewrite, stringName, variableToRefactor, block, oldDeclarationStatement, isAlias, stringType);
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
		if(getProblemId(currentMarker).equals(ProblemIDs.C_STRING_PROBLEM)) {
			IASTSimpleDeclSpecifier ds = DeclaratorTypeAnalyzer.getDeclSpecifier(declarator);
			IASTDeclSpecifier newDeclSpecifier = newRefactoredDeclSpecifier(ds, declarator);
			IASTSimpleDeclaration newDeclaration = ExtendedNodeFactory.newSimpleDeclaration(newDeclSpecifier);
			IASTDeclarator newDeclarator = ExtendedNodeFactory.newDeclarator(declarator.getName().toString());
			
			IASTInitializer initializer;
			if(DeclaratorAnalyzer.hasStrdupAssignment(declarator)) {
				IASTFunctionCallExpression strdupCall = (IASTFunctionCallExpression)DeclaratorAnalyzer.getInitializerClause(declarator);
				initializer = ExtendedNodeFactory.newEqualsInitializer(strdupCall.getArguments()[0].copy());
			}
			else {
				initializer = declarator.getInitializer().copy();
			}
			
			newDeclarator.setInitializer(initializer);
			newDeclaration.addDeclarator(newDeclarator);
			return ExtendedNodeFactory.newDeclarationStatement(newDeclaration);
		}
		else {
			String name = declarator.getName().toString();
			return ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, name, ExtendedNodeFactory.newIntegerLiteral(0));
		}
	}
	
	private IASTDeclSpecifier newRefactoredDeclSpecifier(IASTSimpleDeclSpecifier oldDeclSpecifier, IASTDeclarator oldDeclarator) {
		IASTDeclSpecifier newDeclSpecifier = ExtendedNodeFactory.newNamedTypeSpecifier(DeclaratorAnalyzer.getStringReplacementType(oldDeclarator));
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
