package ch.hsr.ifs.cute.charwars.quickfixes.array;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdArray;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;

public class ArrayQuickFix extends BaseQuickFix {	
	@Override
	public String getLabel() {
		return QuickFixLabels.ARRAY;
	}

	@Override
	protected String getErrorMessage() {
		return ErrorMessages.ARRAY_QUICK_FIX;
	}
	
	@Override
	protected void handleMarkedNode(IASTNode markedNode, ASTRewriteCache rewriteCache) {
		IASTDeclarator oldDeclarator = (IASTDeclarator)markedNode;
		IASTNode block =  ASTAnalyzer.getEnclosingBlock(oldDeclarator);
		boolean isGlobal = (block == oldDeclarator.getTranslationUnit());
		IASTSimpleDeclaration oldDeclaration = (IASTSimpleDeclaration)oldDeclarator.getParent();
		IASTDeclarationStatement oldDeclarationStatement = isGlobal ? null : (IASTDeclarationStatement)oldDeclaration.getParent();
		IASTNode beforeNode = isGlobal ? oldDeclaration : oldDeclarationStatement;
		ASTRewrite rewrite = rewriteCache.getASTRewrite(markedNode.getTranslationUnit().getOriginatingTranslationUnit());

		for(IASTDeclarator declarator : oldDeclaration.getDeclarators()) {
			insertNewDeclarationStatementFromDeclarator(declarator, beforeNode, declarator.equals(oldDeclarator), block, rewrite);
		}
		
		ASTModifier.remove(beforeNode, rewrite);

		IASTName oldName = oldDeclarator.getName();
		ReplaceIdExpressionsVisitor visitor = new ReplaceIdExpressionsVisitor(rewrite, oldName);
		block.accept(visitor);
		
		headers.add(StdArray.HEADER_NAME);
	}

	private String handleStaticDatatypes(String s) {
		final String staticKeyword = "static ";
		
		if(s.contains(staticKeyword)) {
			return staticKeyword + s.replaceAll(staticKeyword, "");	
		}
		return s;
	}
	
	private String getPointerOperators(IASTDeclarator declarator) {
		StringBuffer pointerOperators = new StringBuffer();
		for(IASTPointerOperator p : declarator.getPointerOperators()) {
			pointerOperators.append(p.getRawSignature());
		}
		return pointerOperators.toString();
	}
	
	private String getFirstArrayCount(IASTArrayModifier modifier, IASTDeclarator declarator) {
		IASTExpression constantExpression = modifier.getConstantExpression();
		if(ASTAnalyzer.isIntegerLiteral(constantExpression)) {
			return constantExpression.getRawSignature();
		}
		else {
			ICPPASTInitializerList oldInitializerList = (ICPPASTInitializerList)declarator.getInitializer().getChildren()[0];
			return String.valueOf(oldInitializerList.getClauses().length);
		}
	}
	
	private String getNewName(IASTDeclarator declarator) {
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)declarator.getParent();
		ICPPASTArrayDeclarator arrayDeclarator = (ICPPASTArrayDeclarator)declarator;
		String pointerOperators = getPointerOperators(declarator);
		IASTArrayModifier[] modifiers = arrayDeclarator.getArrayModifiers();
		String firstArrayCount = getFirstArrayCount(modifiers[0], declarator);
		
		String newName = declaration.getDeclSpecifier().getRawSignature() + pointerOperators;
		
		for(int i = modifiers.length - 1; i >= 0; --i) {
			String count = (i == 0) ? firstArrayCount : modifiers[i].getConstantExpression().getRawSignature();
			newName = String.format("%s<%s, %s>", StdArray.STD_ARRAY, newName, count);
		}

		return handleStaticDatatypes(newName);
	}
	
	private IASTDeclarationStatement newRefactoredDeclarationStatementFromDeclarator(IASTDeclarator declarator) {
		String newName = getNewName(declarator);
		ICPPASTNamedTypeSpecifier newNamedTypeSpecifier = ExtendedNodeFactory.newNamedTypeSpecifier(newName);
		IASTSimpleDeclaration newSimpleDeclaration = ExtendedNodeFactory.newSimpleDeclaration(newNamedTypeSpecifier);
		IASTDeclarator newDeclarator = ExtendedNodeFactory.newDeclarator(declarator.getName().toString());
		
		IASTInitializerClause initializerClause = ASTAnalyzer.getInitializerClause(declarator);
		if(initializerClause != null) {
			newDeclarator.setInitializer(ExtendedNodeFactory.newEqualsInitializerWithList(initializerClause.copy()));
		}

		newSimpleDeclaration.addDeclarator(newDeclarator);
		return ExtendedNodeFactory.newDeclarationStatement(newSimpleDeclaration);
	}
	
	private void insertNewDeclarationStatementFromDeclarator(IASTDeclarator declarator, IASTNode beforeNode, boolean isOldDeclarator, IASTNode block, ASTRewrite rewrite) {
		boolean isGlobal = (block == declarator.getTranslationUnit());
		IASTDeclarationStatement declarationStatement = isOldDeclarator ? newRefactoredDeclarationStatementFromDeclarator(declarator) : ExtendedNodeFactory.newDeclarationStatementFromDeclarator(declarator);
		IASTNode nodeToInsert = declarationStatement;
		if(isGlobal) {
			nodeToInsert = declarationStatement.getDeclaration();
		}
		
		ASTModifier.insertBefore(block, beforeNode, nodeToInsert, rewrite);
	}
}
