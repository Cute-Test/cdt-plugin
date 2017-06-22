package ch.hsr.ifs.cute.charwars.quickfixes.cstring.general;

import java.util.function.Function;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.DeclaratorAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdString;
import ch.hsr.ifs.cute.charwars.constants.StringType;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoring;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.BlockRefactoringConfiguration;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.DeclaratorTypeAnalyzer;

public class CStringQuickFix extends BaseQuickFix {
	@Override
	public String getLabel() {
		final String problemId = getProblemId(currentMarker);
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
	protected void handleMarkedNode(IASTNode markedNode, ASTRewrite rewrite, ASTRewriteCache rewriteCache) {
		final IASTDeclarator oldDeclarator = (IASTDeclarator)markedNode;
		final IASTNode block =  ASTAnalyzer.getEnclosingBlock(oldDeclarator);
		final boolean isNotNested = (block == oldDeclarator.getTranslationUnit() ||
				block instanceof ICPPASTNamespaceDefinition ||
				block instanceof IASTCompositeTypeSpecifier );
		final IASTSimpleDeclaration oldDeclaration = (IASTSimpleDeclaration)oldDeclarator.getParent();
		final IASTDeclarationStatement oldDeclarationStatement = isNotNested ? null : (IASTDeclarationStatement)oldDeclaration.getParent();
		final IASTNode beforeNode = isNotNested ? oldDeclaration : oldDeclarationStatement;

		for(final IASTDeclarator declarator : oldDeclaration.getDeclarators()) {
			insertNewDeclarationStatementFromDeclarator(declarator, beforeNode, declarator.equals(oldDeclarator), block, rewrite);
		}

		ASTModifier.remove(beforeNode, rewrite);

		final IASTName varName = oldDeclarator.getName();
		IASTName strName;
		final boolean isAlias = (getProblemId(currentMarker).equals(ProblemIDs.C_STRING_ALIAS_PROBLEM));

		if(isAlias) {
			final IASTInitializerClause initializerClause = DeclaratorAnalyzer.getSingleElementInitializerClause(oldDeclarator.getInitializer());
			final IASTIdExpression idExpression = ASTAnalyzer.getStdStringIdExpression((IASTExpression)initializerClause);
			strName = idExpression.getName();
		} else {
			strName = varName;
		}

		final BlockRefactoringConfiguration config = new BlockRefactoringConfiguration();
		config.setBlock(block);
		config.skipStatement(oldDeclarationStatement);
		config.setASTRewrite(rewrite);
		config.setStringType(StringType.createFromDeclSpecifier(oldDeclaration.getDeclSpecifier()));
		config.setStrName(strName);
		config.setVarName(varName);

		final BlockRefactoring blockRefactoring = new BlockRefactoring(config);
		blockRefactoring.refactorAllStatements();

		headers.addAll(blockRefactoring.getHeadersToInclude());
		headers.add(StdString.HEADER_NAME);
	}

	private int getStringBufferSizeFromDeclarator(IASTDeclarator declarator) {
		int stringBufferSize = -1;
		for(final IASTNode child : declarator.getChildren()) {
			if(child instanceof IASTArrayModifier) {
				final IASTArrayModifier arrayModifier = (IASTArrayModifier)child;
				final IASTExpression constantExpr = arrayModifier.getConstantExpression();
				if(constantExpr != null) {
					stringBufferSize = Integer.parseInt(constantExpr.getRawSignature());
					break;
				}
			}
		}
		return stringBufferSize;
	}

	private static IASTInitializer mapSingleElementInitializer(IASTInitializer initializer, Function<IASTInitializerClause, IASTInitializerClause> transform) {
		final IASTInitializerClause clause = DeclaratorAnalyzer.getSingleElementInitializerClause(initializer);
		if(clause == null) {
			return null;
		}

		final IASTInitializerClause transformedClause = transform.apply(clause);
		if(transformedClause == null) {
			return null;
		}

		if(initializer instanceof IASTEqualsInitializer) {
			final IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			if(equalsInitializer.getInitializerClause() instanceof IASTInitializerList) {
				return ExtendedNodeFactory.newEqualsInitializerWithList(transformedClause);
			} else {
				return ExtendedNodeFactory.newEqualsInitializer(transformedClause);
			}
		} else if(initializer instanceof ICPPASTInitializerList) {
			return ExtendedNodeFactory.newInitializerList(transformedClause);
		} else if(initializer instanceof ICPPASTConstructorInitializer) {
			return ExtendedNodeFactory.newConstructorInitializer(transformedClause);
		}
		return null;
	}

	private IASTDeclarationStatement newRefactoredDeclarationStatementFromDeclarator(IASTDeclarator declarator) {
		if(getProblemId(currentMarker).equals(ProblemIDs.C_STRING_PROBLEM)) {
			final IASTSimpleDeclSpecifier ds = DeclaratorTypeAnalyzer.getDeclSpecifier(declarator);
			final IASTDeclSpecifier newDeclSpecifier = newRefactoredDeclSpecifier(ds, declarator);
			final IASTSimpleDeclaration newDeclaration = ExtendedNodeFactory.newSimpleDeclaration(newDeclSpecifier);
			final IASTDeclarator newDeclarator = ExtendedNodeFactory.newDeclarator(declarator.getName().toString());

			IASTInitializer initializer;
			if(DeclaratorAnalyzer.hasStrdupAssignment(declarator)) {
				initializer = mapSingleElementInitializer(declarator.getInitializer(), clause -> {
					if(clause instanceof IASTFunctionCallExpression) {
						final IASTFunctionCallExpression strdupCall = (IASTFunctionCallExpression)clause;
						return strdupCall.getArguments()[0].copy();
					}
					return null;
				});
			} else {
				initializer = declarator.getInitializer().copy();
			}

			newDeclarator.setInitializer(initializer);
			newDeclaration.addDeclarator(newDeclarator);
			return ExtendedNodeFactory.newDeclarationStatement(newDeclaration);
		} else {
			final String name = declarator.getName().toString();
			final IASTInitializer initializer = mapSingleElementInitializer(declarator.getInitializer(), clause -> {
				if(clause instanceof IASTExpression) {
					final IASTExpression existingOffset = ASTAnalyzer.extractPointerOffset((IASTExpression)clause);
					return (existingOffset != null) ? existingOffset : ExtendedNodeFactory.newIntegerLiteral(0);
				}
				return null;
			});

			return ExtendedNodeFactory.newDeclarationStatement(StdString.STRING_SIZE_TYPE, name, initializer);
		}
	}

	private IASTDeclSpecifier newRefactoredDeclSpecifier(IASTSimpleDeclSpecifier oldDeclSpecifier, IASTDeclarator oldDeclarator) {
		final IASTDeclSpecifier newDeclSpecifier = ExtendedNodeFactory.newNamedTypeSpecifier(DeclaratorAnalyzer.getStringReplacementType(oldDeclarator));
		newDeclSpecifier.setStorageClass(oldDeclSpecifier.getStorageClass());
		final IASTPointerOperator pointerOperators[] = oldDeclarator.getPointerOperators();
		if(pointerOperators.length > 0) {
			final IASTPointer pointer = (IASTPointer)pointerOperators[0];
			newDeclSpecifier.setConst(pointer.isConst() && oldDeclSpecifier.isConst());
		}
		newDeclSpecifier.setVolatile(oldDeclSpecifier.isVolatile());
		return newDeclSpecifier;
	}

	private void insertNewDeclarationStatementFromDeclarator(IASTDeclarator declarator, IASTNode beforeNode, boolean isOldDeclarator, IASTNode block, ASTRewrite rewrite) {
		final boolean isGlobal = (block == declarator.getTranslationUnit());

		IASTNode nodeToInsert = isOldDeclarator ? newRefactoredDeclarationStatementFromDeclarator(declarator) : ExtendedNodeFactory.newDeclarationStatementFromDeclarator(declarator);
		if(isGlobal) {
			nodeToInsert = ((IASTDeclarationStatement)nodeToInsert).getDeclaration();
		}

		ASTModifier.insertBefore(block, beforeNode, nodeToInsert, rewrite);

		if(isOldDeclarator && !isGlobal) {
			final int stringBufferSize = getStringBufferSizeFromDeclarator(declarator);
			if(stringBufferSize != -1) {
				final IASTLiteralExpression new_cap = ExtendedNodeFactory.newIntegerLiteral(stringBufferSize);
				final IASTFunctionCallExpression reserveCall = ExtendedNodeFactory.newMemberFunctionCallExpression(declarator.getName(), StdString.RESERVE, new_cap);
				final IASTExpressionStatement reserveCallStatement = ExtendedNodeFactory.newExpressionStatement(reserveCall);
				ASTModifier.insertBefore(block, beforeNode, reserveCallStatement , rewrite);
			}
		}
	}
}
