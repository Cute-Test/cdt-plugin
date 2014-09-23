package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.constants.Algorithm;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.constants.StringType;
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
		ASTRewrite rewrite = rewriteCache.getASTRewrite(functionCall.getTranslationUnit().getOriginatingTranslationUnit());
		String functionName = ((IASTIdExpression)functionCall.getFunctionNameExpression()).getName().toString();
		functionName = functionName.replaceFirst("^" + Constants.STD_PREFIX, "");
		IASTIdExpression firstArg = (IASTIdExpression)ASTAnalyzer.extractStdStringArg(functionCall.getArguments()[0]);
		IASTNode secondArg = ASTAnalyzer.extractStdStringArg(functionCall.getArguments()[1]);
		
		String searchFunctionName = null;
		for(Function f : functionMap.keySet()) {
			if(f.getName().equals(functionName)) {
				searchFunctionName = functionMap.get(f).getName();
				break;
			}
		}
		IASTFunctionCallExpression searchCall = ExtendedNodeFactory.newMemberFunctionCallExpression(firstArg.getName(), searchFunctionName, secondArg.copy());
		IASTStatement oldStatement = ASTAnalyzer.getStatement(functionCall);
		boolean hasPtrReturnType = !(functionName.equals(CString.STRCSPN) || functionName.equals(CString.STRSPN));
		
		if(hasPtrReturnType) {
			if(functionName.equals(CString.MEMCHR)) {
				performMemchrRefactoring(oldStatement, functionCall, rewrite, firstArg, searchFunctionName, secondArg);
			}
			else if(optimizedRefactoringPossible(oldStatement, hasPtrReturnType)) {
				performOptimizedRefactoringPtrReturnType(oldStatement, functionCall, searchCall, rewrite, firstArg);
			}
			else {
				performNormalRefactoringPtrReturnType(oldStatement, functionCall, searchCall, rewrite, firstArg);
			}
		}
		else {
			if(optimizedRefactoringPossible(oldStatement, hasPtrReturnType)) {
				performOptimizedRefactoringSizeReturnType(oldStatement, functionCall, searchCall, rewrite);
			}
			else {
				performNormalRefactoringSizeReturnType(oldStatement, functionCall, searchCall, rewrite, firstArg);
			}
		}
	}

	private void performMemchrRefactoring(IASTStatement oldStatement, IASTFunctionCallExpression functionCall, ASTRewrite rewrite, IASTIdExpression str, String searchFunctionName, IASTNode secondArg) {
		String posVarName = getPosVarName(functionCall);
		IASTName stringName = str.getName();
		IASTExpression last = null;
		
		if(functionCall.getArguments()[2] instanceof ICPPASTFunctionCallExpression) {
			ICPPASTFunctionCallExpression thirdArg = (ICPPASTFunctionCallExpression) functionCall.getArguments()[2];
			if(ASTAnalyzer.isCallToMemberFunction(thirdArg, Function.SIZE)) {
				if(thirdArg.getChildren()[0] instanceof ICPPASTExpression) {
					IASTIdExpression thirdArgIdExpression = (IASTIdExpression) thirdArg.getChildren()[0].getChildren()[0];
					if(thirdArgIdExpression.getChildren()[0].toString().equals(str.getName().toString())) {
						last = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.END);
					}
				}
			}
		}
		if(last == null) {
			IASTExpression lhs = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);
			last = ExtendedNodeFactory.newPlusExpression(lhs.copy(), (IASTExpression) functionCall.getArguments()[2].copy());
		}
		IASTExpression first = ExtendedNodeFactory.newMemberFunctionCallExpression(stringName, StdString.BEGIN);
		IASTFunctionCallExpression searchCall = ExtendedNodeFactory.newFunctionCallExpression(searchFunctionName, first, last, secondArg.copy());
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
		headers.add(Algorithm.HEADER_NAME);
	}

	private void performNormalRefactoringPtrReturnType(IASTStatement oldStatement, IASTFunctionCallExpression functionCall, IASTFunctionCallExpression searchCall, ASTRewrite rewrite, IASTIdExpression str) {
		String posVarName = getPosVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, posVarName, searchCall);
		IASTConditionalExpression conditionalExpression = newConditionalExpression(posVarName, str);
		
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

	private void performOptimizedRefactoringPtrReturnType(IASTStatement oldStatement, IASTFunctionCallExpression functionCall, IASTFunctionCallExpression searchCall, ASTRewrite rewrite, IASTIdExpression str) {
		String posVarName = getPosVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, posVarName, searchCall);
		IASTNode block = ASTAnalyzer.getEnclosingBlock(oldStatement);
		IASTName name = ((IASTDeclarator)functionCall.getParent().getParent()).getName();
		PtrReturnValueVisitor visitor = new PtrReturnValueVisitor(name, rewrite, posVarName, str.copy());
		block.accept(visitor);	
		
		ASTModifier.insertBefore(oldStatement.getParent(), oldStatement, posVarDS, rewrite);
		ASTModifier.remove(oldStatement, rewrite);
		
	}

	private void performOptimizedRefactoringSizeReturnType(IASTStatement oldStatement, IASTFunctionCallExpression functionCall, IASTFunctionCallExpression searchCall, ASTRewrite rewrite) {
		String resultVarName = getResultVarName(functionCall);
		IASTDeclarationStatement posVarDS = ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, resultVarName, searchCall);
		ASTModifier.replace(oldStatement, posVarDS, rewrite);
		
		IASTNode block = ASTAnalyzer.getEnclosingBlock(oldStatement);
		IASTName name = ((IASTDeclarator)functionCall.getParent().getParent()).getName();
		SizeReturnValueVisitor visitor = new SizeReturnValueVisitor(name, rewrite);
		block.accept(visitor);
	}
	
	private void performNormalRefactoringSizeReturnType(IASTStatement oldStatement, IASTFunctionCallExpression functionCall, IASTFunctionCallExpression searchCall, ASTRewrite rewrite, IASTIdExpression str) {
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

	private IASTFunctionCallExpression getFunctionCallNode(IASTStatement statement) {
		if(statement instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declStatement = (IASTDeclarationStatement)statement;
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)declStatement.getDeclaration();
			for(IASTDeclarator declarator : declaration.getDeclarators()) {
				IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)declarator.getInitializer();
				if(equalsInitializer.getInitializerClause() instanceof IASTFunctionCallExpression)
					return (IASTFunctionCallExpression)equalsInitializer.getInitializerClause();
			}
		}
		else if(statement instanceof IASTExpressionStatement) {
			IASTBinaryExpression assignment = (IASTBinaryExpression)((IASTExpressionStatement)statement).getExpression();
			if(assignment.getOperand2() instanceof IASTFunctionCallExpression)
				return (IASTFunctionCallExpression)assignment.getOperand2();
			if(assignment.getOperand2() instanceof IASTCastExpression) {
				if(assignment.getOperand2().getChildren()[1] instanceof IASTFunctionCallExpression)
					return (IASTFunctionCallExpression) assignment.getOperand2().getChildren()[1];
				if(assignment.getOperand2().getChildren()[1] instanceof IASTUnaryExpression && 
						assignment.getOperand2().getChildren()[1].getChildren()[0] instanceof IASTFunctionCallExpression)
					return (IASTFunctionCallExpression) assignment.getInitOperand2().getChildren()[1].getChildren()[0];
			}
		}
		return null;
	}
	
	
	private String getResultVarName(IASTFunctionCallExpression functionCall) {
		String resultVarName = null;
		IASTNode parent = functionCall.getParent();
		
		if(parent instanceof IASTEqualsInitializer) {
			IASTDeclarator declarator = (IASTDeclarator)parent.getParent();
			resultVarName = declarator.getName().toString();
		}
		else if(parent instanceof IASTUnaryExpression && 
				parent.getParent() instanceof IASTCastExpression &&
				parent.getParent().getParent() instanceof IASTEqualsInitializer) {
			IASTDeclarator declarator = (IASTDeclarator)parent.getParent().getParent().getParent();
			resultVarName = declarator.getName().toString();
		} 
		else if(parent instanceof IASTCastExpression &&
				parent.getParent() instanceof IASTEqualsInitializer) {
			IASTDeclarator declarator = (IASTDeclarator)parent.getParent().getParent();
			resultVarName = declarator.getName().toString();
		}
		else if(ASTAnalyzer.isAssignment(parent) || 
				(parent instanceof IASTCastExpression && ASTAnalyzer.isAssignment(parent.getParent())) ||
				(parent instanceof IASTUnaryExpression && parent.getParent() instanceof IASTCastExpression && 
						ASTAnalyzer.isAssignment(parent.getParent().getParent()))) {
			IASTBinaryExpression assignment;
			if(parent.getParent() instanceof IASTCastExpression) {
				assignment = (IASTBinaryExpression)parent.getParent().getParent(); 
			} 
			else if(parent instanceof IASTCastExpression) {
				assignment = (IASTBinaryExpression)parent.getParent();
			} 
			else {
				assignment = (IASTBinaryExpression)parent;
			}
			
			IASTExpression lvalue = assignment.getOperand1();
			if(lvalue instanceof IASTIdExpression) {
				resultVarName = ((IASTIdExpression)lvalue).getName().toString();
			}
			else if(lvalue instanceof IASTFieldReference) {
				resultVarName = ((IASTFieldReference)lvalue).getFieldName().toString();
			}
			else if(lvalue instanceof IASTArraySubscriptExpression) {
				resultVarName = ((IASTIdExpression)((IASTArraySubscriptExpression)lvalue).getArrayExpression()).getName().toString();
			}
		}
		return resultVarName;
	}
	
	private String getPosVarName(IASTFunctionCallExpression functionCall) {
		String posVarName = getResultVarName(functionCall) + "_pos";
		
		if(!ASTAnalyzer.isNameAvailable(posVarName, functionCall)) {
			int counter = 2;
			while(!ASTAnalyzer.isNameAvailable(posVarName + counter, functionCall)) {
				counter++;
			}
			posVarName += counter;
		}
		
		return posVarName;
	}
	
	private IASTConditionalExpression newConditionalExpression(String posVarName, IASTIdExpression strNode) {
		IASTIdExpression posVar = ExtendedNodeFactory.newIdExpression(posVarName);
		IASTExpression condition = ExtendedNodeFactory.newEqualityComparison(posVar, ExtendedNodeFactory.newNposExpression(StringType.STRING), false);
		IASTExpression positive = ExtendedNodeFactory.newAdressOperatorExpression(ExtendedNodeFactory.newArraySubscriptExpression(strNode.copy(), posVar));
		IASTExpression negative = ExtendedNodeFactory.newIdExpression(Constants.NULLPTR);
		return ExtendedNodeFactory.newConditionalExpression(condition, positive, negative);
	}
	
	private IASTConditionalExpression newMemchrConditionalExpression(String posVarName, IASTIdExpression strNode, IASTExpression last) {
		IASTIdExpression posVar = ExtendedNodeFactory.newIdExpression(posVarName);
		IASTExpression condition = ExtendedNodeFactory.newEqualityComparison(posVar, last, false);
		IASTExpression positive = ExtendedNodeFactory.newAdressOperatorExpression(ExtendedNodeFactory.newDereferenceOperatorExpression(posVar));
		IASTExpression negative = ExtendedNodeFactory.newIdExpression(Constants.NULLPTR);
		return ExtendedNodeFactory.newConditionalExpression(condition, positive, negative);
	}
	
	private boolean optimizedRefactoringPossible(IASTStatement statement, boolean ptrReturnType) {
		if(!(statement instanceof IASTDeclarationStatement)) 
			return false;
		IASTName varName = ((IASTSimpleDeclaration)((IASTDeclarationStatement)statement).getDeclaration()).getDeclarators()[0].getName();
		IASTNode block = ASTAnalyzer.getEnclosingBlock(statement);
		OptimizationCheckerVisitor visitor = new OptimizationCheckerVisitor(varName, ptrReturnType);
		block.accept(visitor);
		return visitor.isOptimizationPossible();
	}
}
