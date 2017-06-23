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
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder;
import ch.hsr.ifs.cute.charwars.asttools.IndexFinder.IndexFinderInstruction;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoringConfiguration;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.DeclaratorTypeAnalyzer;
import ch.hsr.ifs.cute.charwars.utils.analyzers.FunctionAnalyzer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class RewriteStrategy {
	protected ASTRewriteCache rewriteCache;
	protected IASTFunctionDefinition functionDefinition;
	protected ICPPASTParameterDeclaration strParameter;
	protected IASTName strName;
	@SuppressFBWarnings(value="URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	protected IASTStatement statements[];

	public final void setRewriteCache(ASTRewriteCache rewriteCache) {
		this.rewriteCache = rewriteCache;
	}

	protected ASTRewrite getMainRewrite() {
		return rewriteCache.getASTRewrite(strName.getTranslationUnit().getOriginatingTranslationUnit());
	}

	public final void setStrParameter(ICPPASTParameterDeclaration strParameter) {
		this.strParameter = strParameter;
		this.strName = strParameter.getDeclarator().getName();
		this.functionDefinition = (IASTFunctionDefinition)strParameter.getParent().getParent();
	}

	public final void setStatements(IASTStatement statements[]) {
		this.statements = statements.clone();
	}

	public final void addStdStringOverload() {
		final ASTRewrite subrewrite1 = duplicateFunction(getMainRewrite());
		adaptStrParameter(subrewrite1);
		adaptFunctionBody(subrewrite1);
	}

	public abstract void adaptCStringOverload();

	public final void addNewDeclarations() {
		final int strParameterIndex = FunctionAnalyzer.getParameterIndex(strParameter);
		final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)strParameter.getParent();

		IndexFinder.findDeclarations(functionDeclarator.getName(), rewriteCache, (declarationFunctionName, rewrite) -> {
			final ICPPASTFunctionDeclarator functionDeclarator1 = (ICPPASTFunctionDeclarator)declarationFunctionName.getParent();
			if(functionDeclarator1.getParent() instanceof IASTSimpleDeclaration) {
				final IASTSimpleDeclaration oldFunctionDeclaration = (IASTSimpleDeclaration)functionDeclarator1.getParent();
				final ASTRewrite subrewrite = rewrite.insertBefore(oldFunctionDeclaration.getParent(), oldFunctionDeclaration, oldFunctionDeclaration, null);
				final ICPPASTParameterDeclaration oldParameterDeclaration = functionDeclarator1.getParameters()[strParameterIndex];
				final ICPPASTParameterDeclaration newParameterDeclaration = newAdaptedParameterDeclaration(oldParameterDeclaration);
				ASTModifier.replace(oldParameterDeclaration, newParameterDeclaration, subrewrite);
			}
			return IndexFinderInstruction.CONTINUE_SEARCH;
		});
	}

	private ASTRewrite duplicateFunction(ASTRewrite rewrite) {
		return rewrite.insertBefore(functionDefinition.getParent(), functionDefinition, functionDefinition, null);
	}

	private ASTRewrite adaptStrParameter(ASTRewrite rewrite) {
		final ICPPASTParameterDeclaration adaptedStrParameter = newAdaptedParameterDeclaration(strParameter);
		return rewrite.replace(strParameter, adaptedStrParameter, null);
	}

	private ASTRewrite adaptFunctionBody(ASTRewrite rewrite) {
		//replace function body
		final IASTCompoundStatement originalFunctionBody = (IASTCompoundStatement)functionDefinition.getBody();
		final IASTCompoundStatement stdStringOverloadBody = getStdStringOverloadBody();
		final ASTRewrite subrewrite = rewrite.replace(originalFunctionBody, stdStringOverloadBody, null);

		//adapt variable occurrences
		final BlockRefactoringConfiguration config = new BlockRefactoringConfiguration();
		config.setBlock(stdStringOverloadBody);
		config.setASTRewrite(subrewrite);
		config.setStringType(StringType.createFromDeclSpecifier(strParameter.getDeclSpecifier()));
		config.setStrName(strName);
		config.setVarName(strName);

		final BlockRefactoring blockRefactoring = new BlockRefactoring(config);
		blockRefactoring.refactorAllStatements();
		return subrewrite;
	}

	protected abstract IASTCompoundStatement getStdStringOverloadBody();

	private ICPPASTParameterDeclaration newAdaptedParameterDeclaration(ICPPASTParameterDeclaration parameterDeclaration) {
		final IASTDeclSpecifier declSpecifier = ExtendedNodeFactory.newNamedTypeSpecifier(StdString.STD_STRING);
		declSpecifier.setConst(true);
		final IASTDeclarator declarator = ExtendedNodeFactory.newReferenceDeclarator(parameterDeclaration.getDeclarator().getName().toString());
		return ExtendedNodeFactory.newParameterDeclaration(declSpecifier, declarator);
	}

	protected final IASTStatement getStdStringFunctionCallStatement() {
		final IASTDeclarator functionDeclarator = functionDefinition.getDeclarator();
		final IASTFunctionCallExpression stdStringOverloadFunctionCall = getStdStringFunctionCallExpression();

		IASTStatement statement;
		if(DeclaratorTypeAnalyzer.hasVoidType(functionDeclarator)) {
			statement = ExtendedNodeFactory.newExpressionStatement(stdStringOverloadFunctionCall);
		} else {
			statement = ExtendedNodeFactory.newReturnStatement(stdStringOverloadFunctionCall);
		}

		return statement;
	}

	protected final IASTFunctionCallExpression getStdStringFunctionCallExpression() {
		final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator)strParameter.getParent();
		final List<IASTNode> arguments = new ArrayList<>();

		for(final ICPPASTParameterDeclaration parameterDeclaration : functionDeclarator.getParameters()) {
			final IASTIdExpression idExpression = ExtendedNodeFactory.newIdExpression(parameterDeclaration.getDeclarator().getName().toString());
			if(parameterDeclaration == strParameter) {
				arguments.add(ExtendedNodeFactory.newFunctionCallExpression(StdString.STD_STRING, idExpression));
			} else {
				arguments.add(idExpression);
			}
		}

		final String functionNameStr = functionDeclarator.getName().toString();
		final IASTNode argumentsArr[] = arguments.toArray(new IASTNode[]{});
		return ExtendedNodeFactory.newFunctionCallExpression(functionNameStr, argumentsArr);
	}
}
