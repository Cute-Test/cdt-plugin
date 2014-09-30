package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder.IndexFinderInstruction;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder.ResultHandler;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoring;
import ch.hsr.ifs.cute.charwars.utils.DeclaratorTypeAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.FunctionAnalyzer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class RewriteStrategy {
	protected ASTRewrite rewrite;
	protected IASTFunctionDefinition functionDefinition;
	protected ICPPASTParameterDeclaration strParameter;
	protected IASTName strName;
	@SuppressFBWarnings(value="URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	protected IASTStatement statements[];
	
	public void setRewrite(ASTRewrite rewrite) {
		this.rewrite = rewrite;
	}
	
	public void setStrParameter(ICPPASTParameterDeclaration strParameter) {
		this.strParameter = strParameter;
		this.strName = strParameter.getDeclarator().getName();
		this.functionDefinition = (IASTFunctionDefinition)strParameter.getParent().getParent();
	}
	
	public void setStatements(IASTStatement statements[]) {
		this.statements = statements.clone();
	}
	
	public void addStdStringOverload() {
		ASTRewrite subrewrite1 = duplicateFunction(rewrite);
		adaptStrParameter(subrewrite1);
		adaptFunctionBody(subrewrite1);
	}
	
	public abstract void adaptCStringOverload();
	
	public void addNewDeclarations(ASTRewriteCache rewriteCache) {
		final int strParameterIndex = FunctionAnalyzer.getParameterIndex(strParameter);
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)strParameter.getParent();

		IndexFinder.findDeclarations(functionDeclarator.getName(), rewriteCache, new ResultHandler() {
			@Override
			public IndexFinderInstruction handleResult(IASTName declarationFunctionName, ASTRewrite rewrite) {
				ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)declarationFunctionName.getParent();
				if(functionDeclarator.getParent() instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration oldFunctionDeclaration = (IASTSimpleDeclaration)functionDeclarator.getParent();
					ASTRewrite subrewrite = rewrite.insertBefore(oldFunctionDeclaration.getParent(), oldFunctionDeclaration, oldFunctionDeclaration, null);
					ICPPASTParameterDeclaration oldParameterDeclaration = functionDeclarator.getParameters()[strParameterIndex];
					ICPPASTParameterDeclaration newParameterDeclaration = newAdaptedParameterDeclaration(oldParameterDeclaration);
					ASTModifier.replace(oldParameterDeclaration, newParameterDeclaration, subrewrite);
				}
				return IndexFinderInstruction.CONTINUE_SEARCH;
			}
		});
	}
	
	private ASTRewrite duplicateFunction(ASTRewrite rewrite) {
		return rewrite.insertBefore(functionDefinition.getParent(), functionDefinition, functionDefinition, null);
	}

	private ASTRewrite adaptStrParameter(ASTRewrite rewrite) {
		ICPPASTParameterDeclaration adaptedStrParameter = newAdaptedParameterDeclaration(strParameter);
		return rewrite.replace(strParameter, adaptedStrParameter, null);
	}
	
	private ASTRewrite adaptFunctionBody(ASTRewrite rewrite) {
		//replace function body
		IASTCompoundStatement originalFunctionBody = (IASTCompoundStatement)functionDefinition.getBody();
		IASTCompoundStatement stdStringOverloadBody = getStdStringOverloadBody();
		ASTRewrite subrewrite = rewrite.replace(originalFunctionBody, stdStringOverloadBody, null);
		//subrewrite = subrewrite.insertBefore(stdStringOverloadBody, stdStringOverloadBody.getStatements()[0], ExtendedNodeFactory.newExpressionStatement(ExtendedNodeFactory.newNposExpression()), null);
		//ASTModifier.insertBefore(stdStringOverloadBody, stdStringOverloadBody., ExtendedNodeFactory.factory.newExpressionStatement(null), subrewrite);
		
		//adapt variable occurrences
		StringType stringType = StringType.createFromDeclSpecifier(strParameter.getDeclSpecifier());
		BlockRefactoring blockRefactoring = new BlockRefactoring(subrewrite, strName.toString(), strName, stdStringOverloadBody, null, false, stringType);
		blockRefactoring.refactorAllStatements();
		return subrewrite;
	}
	
	protected abstract IASTCompoundStatement getStdStringOverloadBody();
	
	private ICPPASTParameterDeclaration newAdaptedParameterDeclaration(ICPPASTParameterDeclaration parameterDeclaration) {
		IASTDeclSpecifier declSpecifier = ExtendedNodeFactory.newNamedTypeSpecifier(StdString.STD_STRING);
		declSpecifier.setConst(true);
		IASTDeclarator declarator = ExtendedNodeFactory.newReferenceDeclarator(parameterDeclaration.getDeclarator().getName().toString());
		return ExtendedNodeFactory.newParameterDeclaration(declSpecifier, declarator);
	}
	
	protected IASTStatement getStdStringFunctionCallStatement() {
		IASTDeclarator functionDeclarator = functionDefinition.getDeclarator();
		IASTFunctionCallExpression stdStringOverloadFunctionCall = getStdStringFunctionCallExpression();
		
		IASTStatement statement;
		if(DeclaratorTypeAnalyzer.hasVoidType(functionDeclarator)) {
			statement = ExtendedNodeFactory.newExpressionStatement(stdStringOverloadFunctionCall);		
		}
		else {
			statement = ExtendedNodeFactory.newReturnStatement(stdStringOverloadFunctionCall);
		}
		
		return statement;
	}
	
	protected IASTFunctionCallExpression getStdStringFunctionCallExpression() {
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)strParameter.getParent();
		List<IASTNode> arguments = new ArrayList<IASTNode>();
		
		for(ICPPASTParameterDeclaration parameterDeclaration : functionDeclarator.getParameters()) {
			IASTIdExpression idExpression = ExtendedNodeFactory.newIdExpression(parameterDeclaration.getDeclarator().getName().toString());
			if(parameterDeclaration == strParameter) {
				arguments.add(ExtendedNodeFactory.newFunctionCallExpression(StdString.STD_STRING, idExpression));
			}
			else {
				arguments.add(idExpression);
			}
		}
		
		String functionNameStr = functionDeclarator.getName().toString();
		IASTNode argumentsArr[] = arguments.toArray(new IASTNode[]{});
		return ExtendedNodeFactory.newFunctionCallExpression(functionNameStr, argumentsArr);
	}
}
