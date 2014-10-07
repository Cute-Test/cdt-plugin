package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.constants.Algorithm;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Function;

public class CStringCleanupQuickFix extends BaseQuickFix {
	public static final Map<Function, Function> functionMap;
	
	static {
		functionMap = new HashMap<Function, Function>();
		functionMap.put(Function.STRSTR, Function.FIND);
		functionMap.put(Function.STRCHR, Function.FIND);
		functionMap.put(Function.STRRCHR, Function.RFIND);
		functionMap.put(Function.STRPBRK, Function.FIND_FIRST_OF);
		functionMap.put(Function.STRCSPN, Function.FIND_FIRST_OF);
		functionMap.put(Function.STRSPN, Function.FIND_FIRST_NOT_OF);
		functionMap.put(Function.MEMCHR, Function.STD_FIND);
	}
	
	@Override
	public String getLabel() {
		return QuickFixLabels.C_STRING_CLEANUP;
	}
	
	@Override
	protected String getErrorMessage() {
		return ErrorMessages.C_STRING_CLEANUP_QUICK_FIX;
	}
	
	@Override
	protected void handleMarkedNode(IASTNode markedNode, ASTRewriteCache rewriteCache) {
		IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression)markedNode;
		ASTRewrite rewrite = getRewrite(rewriteCache, functionCall);
		
		IASTIdExpression firstArg = (IASTIdExpression)ASTAnalyzer.extractStdStringArg(functionCall.getArguments()[0]);
		IASTNode secondArg = ASTAnalyzer.extractStdStringArg(functionCall.getArguments()[1]);
		
		Function function = getFunction(functionCall);
		String searchFunctionName = functionMap.get(function).getName();
		IASTFunctionCallExpression searchCall = ExtendedNodeFactory.newMemberFunctionCallExpression(firstArg.getName(), searchFunctionName, secondArg.copy());
		IASTStatement oldStatement = ASTAnalyzer.getStatement(functionCall);
		boolean hasPtrReturnType = !(function == Function.STRCSPN || function == Function.STRSPN);
		
		CleanupRefactoring refactoring = null;
		if(function == Function.MEMCHR) {
			refactoring = new MemchrCleanupRefactoring(functionCall, searchCall, rewrite);
		}
		else if(hasPtrReturnType) {
			refactoring = new PtrCleanupRefactoring(functionCall, searchCall, rewrite);
		}
		else {
			refactoring = new SizeCleanupRefactoring(functionCall, searchCall, rewrite);
		}
		
		if(optimizedRefactoringPossible(oldStatement, hasPtrReturnType, function)) {
			refactoring.performOptimized();
		}
		else {
			refactoring.performNormal();
			if(function == Function.MEMCHR) {
				headers.add(Algorithm.HEADER_NAME);
			}
		}
	}
	
	private Function getFunction(IASTFunctionCallExpression functionCall) {
		IASTIdExpression functionNameExpr = (IASTIdExpression)functionCall.getFunctionNameExpression();
		String functionName = functionNameExpr.getName().toString().replaceFirst("^" + Constants.STD_PREFIX, "");
		
		for(Function f : functionMap.keySet()) {
			if(f.getName().equals(functionName)) {
				return f;
			}
		}
		return null;
	}
	
	private boolean optimizedRefactoringPossible(IASTStatement statement, boolean ptrReturnType, Function function) {
		if(!(statement instanceof IASTDeclarationStatement) || function == Function.MEMCHR) 
			return false;
		IASTName varName = ((IASTSimpleDeclaration)((IASTDeclarationStatement)statement).getDeclaration()).getDeclarators()[0].getName();
		IASTNode block = ASTAnalyzer.getEnclosingBlock(statement);
		OptimizationCheckerVisitor visitor = new OptimizationCheckerVisitor(varName, ptrReturnType);
		block.accept(visitor);
		return visitor.isOptimizationPossible();
	}
}
