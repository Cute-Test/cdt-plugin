package ch.hsr.ifs.cute.charwars.quickfixes.pointerparameter;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder.IndexFinderInstruction;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder.ResultHandler;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.UEAnalyzer;

public class PointerParameterQuickFix extends BaseQuickFix {
	@Override
	public String getLabel() {
		return QuickFixLabels.POINTER_PARAMETER;
	}
	
	@Override
	protected String getErrorMessage() {
		return ErrorMessages.POINTER_PARAMETER_QUICK_FIX;
	}
	
	@Override
	protected void handleMarkedNode(IASTNode markedNode, ASTRewriteCache rewriteCache) {
		IASTDeclarator paramDeclarator = (IASTDeclarator)markedNode;
		ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration)paramDeclarator.getParent();
		final int paramIndex = FunctionAnalyzer.getParameterIndex(parameterDeclaration);
		IASTFunctionDeclarator functionDeclarator = (IASTFunctionDeclarator)parameterDeclaration.getParent();
		
		IndexFinder.findAllOccurrences(functionDeclarator.getName(), rewriteCache, new ResultHandler() {
			@Override
			public IndexFinderInstruction handleResult(IASTName name, ASTRewrite rewrite) {
				handleNode(name, paramIndex, rewrite);
				return IndexFinderInstruction.CONTINUE_SEARCH;
			}
		});
	}

	private void handleNode(IASTNode functionName, int paramIndex, ASTRewrite rewrite) {
		IASTNode node = functionName;
		while(node != null && 
			!(node instanceof IASTFunctionDefinition) &&
			!(node instanceof IASTFunctionCallExpression) &&
			!(node instanceof IASTSimpleDeclaration)) {
			
			node = node.getParent();
		}
		
		if(node instanceof IASTFunctionDefinition)
			handleFunctionDefinition((IASTFunctionDefinition)node, paramIndex, rewrite);
		else if(node instanceof IASTFunctionCallExpression)
			handleFunctionCall((IASTFunctionCallExpression)node, paramIndex, rewrite);
		else if(node instanceof IASTSimpleDeclaration)
			handleFunctionDeclaration((IASTSimpleDeclaration)node, paramIndex, rewrite);
	}
	
	private void handleFunctionDefinition(IASTFunctionDefinition functionDefinition, int paramIndex, ASTRewrite rewrite) {
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)functionDefinition.getDeclarator();
		IASTParameterDeclaration oldParamDeclaration = functionDeclarator.getParameters()[paramIndex];
		IASTDeclarator oldDeclarator = oldParamDeclaration.getDeclarator();
		ICPPASTDeclarator newDeclarator = ExtendedNodeFactory.newReferenceDeclarator(oldDeclarator.getName().toString());
		IASTDeclSpecifier newDeclSpecifier = oldParamDeclaration.getDeclSpecifier().copy();
		IASTParameterDeclaration newParamDeclaration = ExtendedNodeFactory.newParameterDeclaration(newDeclSpecifier, newDeclarator);
		ASTModifier.replace(oldParamDeclaration, newParamDeclaration, rewrite);
		
		IASTStatement functionBody = functionDefinition.getBody();
		ReplaceInsideFunctionBodyVisitor visitor = new ReplaceInsideFunctionBodyVisitor(rewrite, oldDeclarator.getName());
		functionBody.accept(visitor);
	}
	
	private void handleFunctionCall(IASTFunctionCallExpression fcExpression, int paramIndex, ASTRewrite rewrite) {
		IASTNode arg = fcExpression.getArguments()[paramIndex];
		if(UEAnalyzer.isAddressOperatorExpression(arg)) {
			IASTNode newArg = UEAnalyzer.getOperand(arg).copy();
			ASTModifier.replace(arg, newArg, rewrite);
		}
		else if(arg instanceof IASTIdExpression) {
			IASTIdExpression idExpression = (IASTIdExpression)arg;
			IASTUnaryExpression newArg = ExtendedNodeFactory.newDereferenceOperatorExpression(idExpression.copy());
			ASTModifier.replace(idExpression, newArg, rewrite);
		}
	}
	
	private void handleFunctionDeclaration(IASTSimpleDeclaration simpleDeclaration, int paramIndex, ASTRewrite rewrite) {
		for(IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
			if(declarator instanceof ICPPASTFunctionDeclarator) {
				ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)declarator;
				IASTParameterDeclaration param = functionDeclarator.getParameters()[paramIndex];
				IASTDeclSpecifier newDeclSpecifier = param.getDeclSpecifier().copy();
				IASTDeclarator newDeclarator = ExtendedNodeFactory.newReferenceDeclarator(param.getDeclarator().getName().toString());
				IASTParameterDeclaration newParam = ExtendedNodeFactory.newParameterDeclaration(newDeclSpecifier, newDeclarator);
				ASTModifier.replace(param, newParam, rewrite);
				return;
			}
		}
	}
}