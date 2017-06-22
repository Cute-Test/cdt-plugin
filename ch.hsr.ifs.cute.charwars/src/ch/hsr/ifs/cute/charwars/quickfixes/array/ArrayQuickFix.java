package ch.hsr.ifs.cute.charwars.quickfixes.array;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.constants.QuickFixLabels;
import ch.hsr.ifs.cute.charwars.constants.StdArray;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFix;
import ch.hsr.ifs.cute.charwars.utils.ExtendedNodeFactory;
import ch.hsr.ifs.cute.charwars.utils.analyzers.LiteralAnalyzer;

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
	protected void handleMarkedNode(IASTNode markedNode, ASTRewrite rewrite, ASTRewriteCache rewriteCache) {
		final IASTDeclarator oldDeclarator = (IASTDeclarator)markedNode;
		final IASTNode block = ASTAnalyzer.getEnclosingBlock(oldDeclarator);

		final IASTSimpleDeclaration oldDeclaration = (IASTSimpleDeclaration)oldDeclarator.getParent();
		final boolean mustInsertInsteadOfReplace = oldDeclaration.getDeclarators().length > 1;
		final IASTDeclarationStatement oldDeclarationStatement = mustInsertInsteadOfReplace ? getOldDeclarationStatementIfExistent(oldDeclaration) : null;

		if(mustInsertInsteadOfReplace) {
			IASTNode beforeNode = oldDeclaration;
			final boolean requiresDeclarationStatement = oldDeclarationStatement != null;
			for(final IASTDeclarator declarator : oldDeclaration.getDeclarators()) {
				final IASTSimpleDeclaration simpleDeclaration = newRefactoredSimpleDeclarationFromDeclarator(declarator);
				if(simpleDeclaration == null) {
					return;
				}
				IASTNode nodeToInsert = simpleDeclaration;
				if(requiresDeclarationStatement) {
					beforeNode = oldDeclarationStatement;
					nodeToInsert = ExtendedNodeFactory.newDeclarationStatement(simpleDeclaration);
				}
				ASTModifier.insertBefore(block, beforeNode, nodeToInsert, rewrite);
			}
			ASTModifier.remove(beforeNode, rewrite);
		} else {
			final IASTDeclarator declarator = oldDeclarator;
			final IASTSimpleDeclaration simpleDeclaration = newRefactoredSimpleDeclarationFromDeclarator(declarator);
			if(simpleDeclaration == null) {
				return;
			}
			ASTModifier.replace(oldDeclaration, simpleDeclaration, rewrite);
		}

		final IASTName oldName = oldDeclarator.getName();
		final ReplaceIdExpressionsVisitor visitor = new ReplaceIdExpressionsVisitor(rewrite, oldName);
		block.accept(visitor);

		headers.add(StdArray.HEADER_NAME);
	}

	private IASTDeclarationStatement getOldDeclarationStatementIfExistent(IASTSimpleDeclaration oldDeclaration) {
		final IASTNode parent = oldDeclaration.getParent();
		return parent instanceof IASTDeclarationStatement ? (IASTDeclarationStatement)parent : null;
	}

	private IASTExpression getArrayCountExpression(IASTArrayModifier modifier, IASTDeclarator declarator) {
		final IASTExpression constantExpression = modifier.getConstantExpression(); // assumes to be copied with location info
		if(LiteralAnalyzer.isInteger(constantExpression)) {
			return constantExpression;
		} else {
			final IASTInitializerClause initializerClause = getArrayInitializerClause(declarator);
			if(initializerClause instanceof IASTInitializerList) {
				final IASTInitializerList initializerList = (IASTInitializerList)initializerClause;
				final String dimensionFromInitializer = String.valueOf(initializerList.getClauses().length);
				return ExtendedNodeFactory.newLiteralExpression(dimensionFromInitializer);
			}
		}
		return null;
	}

	private IASTSimpleDeclaration newRefactoredSimpleDeclarationFromDeclarator(IASTDeclarator declarator) {
		final IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)declarator.getParent();
		final boolean isArrayDeclarator = declarator instanceof IASTArrayDeclarator;
		final IASTDeclarator declaratorCopy = declarator.copy(CopyStyle.withLocations);
		final IASTDeclSpecifier declSpecCopy = declaration.getDeclSpecifier().copy(CopyStyle.withLocations);
		final int storageclass = declSpecCopy.getStorageClass();
		declSpecCopy.setStorageClass(IASTDeclSpecifier.sc_unspecified);

		IASTSimpleDeclaration newSimpleDeclaration;
		IASTDeclarator newDeclarator;
		if(isArrayDeclarator) {
			final IASTArrayDeclarator arrayDeclarator = (IASTArrayDeclarator)declaratorCopy;
			IASTDeclarator nested = arrayDeclarator.getNestedDeclarator();
			if(nested == null) {
				nested = ExtendedNodeFactory.newDeclarator();
			}
			final IASTPointerOperator[] pointerOperators = arrayDeclarator.getPointerOperators();
			for(final IASTPointerOperator operator : pointerOperators) {
				if(operator != null) {
					nested.addPointerOperator(operator);
				}
			}
			final IASTArrayModifier[] modifiers = arrayDeclarator.getArrayModifiers();
			ICPPASTNamedTypeSpecifier newNamedTypeSpecifier=null;
			IASTTypeId newTypeId = ExtendedNodeFactory.newTypeId(declSpecCopy, nested);
			for(int i = modifiers.length - 1; i >= 0; --i) {
				final IASTArrayModifier modifier = modifiers[i];
				final IASTExpression dimension = getArrayCountExpression(modifier, arrayDeclarator);
				if(dimension == null) {
					return null;
				}

				newNamedTypeSpecifier = makeStdArray(newTypeId, dimension);
				if(i > 0) {
					newTypeId = ExtendedNodeFactory.newIASTTypeId(newNamedTypeSpecifier);
				}
			}
			newSimpleDeclaration = ExtendedNodeFactory.newSimpleDeclaration(newNamedTypeSpecifier);
			newDeclarator = ExtendedNodeFactory.newDeclarator(arrayDeclarator.getName().toString());
		} else {
			// no array declarator, just use the copy
			newSimpleDeclaration = ExtendedNodeFactory.newSimpleDeclaration(declSpecCopy);
			newDeclarator = declaratorCopy;
		}

		final IASTInitializer newInitializer = adaptInitializer(declarator, isArrayDeclarator);
		if(newInitializer != null) {
			newDeclarator.setInitializer(newInitializer);
		}

		newSimpleDeclaration.addDeclarator(newDeclarator);
		newSimpleDeclaration.getDeclSpecifier().setStorageClass(storageclass); // re-establish original storage class
		return newSimpleDeclaration;
	}

	private IASTInitializer adaptInitializer(IASTDeclarator declarator, boolean isArrayDeclarator) {
		IASTInitializerClause initializerClause = getArrayInitializerClause(declarator);
		if(initializerClause == null) {
			return null;
		}

		initializerClause = initializerClause.copy(CopyStyle.withLocations);
		if(isArrayDeclarator) {
			return ExtendedNodeFactory.newEqualsInitializerWithList(initializerClause);
		} else {
			return ExtendedNodeFactory.newEqualsInitializer(initializerClause);
		}
	}

	private ICPPASTNamedTypeSpecifier makeStdArray(IASTTypeId newTypeId, IASTExpression dimension) {
		final ICPPASTName std = ExtendedNodeFactory.newName(StdArray.STD);
		final ICPPASTName array = ExtendedNodeFactory.newName(StdArray.ARRAY);
		final ICPPASTTemplateId templateId = ExtendedNodeFactory.newTemplateId(array);
		templateId.addTemplateArgument(newTypeId);
		templateId.addTemplateArgument(dimension);
		final ICPPASTQualifiedName qualifiedName = ExtendedNodeFactory.newQualifiedName(std);
		qualifiedName.addName(templateId);
		return ExtendedNodeFactory.newTypedefNameSpecifier(qualifiedName);
	}

	private static IASTInitializerClause getArrayInitializerClause(IASTDeclarator declarator) {
		final IASTInitializer initializer = declarator.getInitializer();
		if(initializer instanceof IASTEqualsInitializer) {
			final IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer)initializer;
			return equalsInitializer.getInitializerClause();
		} else if(initializer instanceof IASTInitializerList) {
			return (IASTInitializerList)initializer;
		}
		return null;
	}
}
