package ch.hsr.ifs.cute.charwars.quickfixes.array;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.asttools.DeclaratorAnalyzer;
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
		IASTDeclarator oldDeclarator = (IASTDeclarator)markedNode;
		IASTNode block =  ASTAnalyzer.getEnclosingBlock(oldDeclarator);
		// logic is wrong for many cases
		IASTSimpleDeclaration oldDeclaration = (IASTSimpleDeclaration)oldDeclarator.getParent();
		boolean mustInsertInsteadOfReplace = oldDeclaration.getDeclarators().length>1;
		IASTDeclarationStatement oldDeclarationStatement = mustInsertInsteadOfReplace? getOldDeclarationStatementIfExistent(oldDeclaration):null;
		IASTNode beforeNode = oldDeclaration ;

		if (mustInsertInsteadOfReplace){
			boolean requiresdeclarationstatement = oldDeclarationStatement != null;
		
			for (IASTDeclarator declarator : oldDeclaration.getDeclarators()) {
				IASTSimpleDeclaration simpleDeclaration = newRefactoredSimpleDeclarationFromDeclarator(declarator);
				IASTNode nodeToInsert = simpleDeclaration;
				if (requiresdeclarationstatement) {
					beforeNode = oldDeclarationStatement;
					nodeToInsert = ExtendedNodeFactory.newDeclarationStatement(simpleDeclaration);
				}
				ASTModifier.insertBefore(block, beforeNode, nodeToInsert, rewrite);
			}
			ASTModifier.remove(beforeNode, rewrite);
		} else {
			IASTDeclarator declarator = oldDeclarator; 
			IASTSimpleDeclaration simpleDeclaration = newRefactoredSimpleDeclarationFromDeclarator(declarator);
			ASTModifier.replace(beforeNode, simpleDeclaration, rewrite);
		}

		IASTName oldName = oldDeclarator.getName();
		ReplaceIdExpressionsVisitor visitor = new ReplaceIdExpressionsVisitor(rewrite, oldName);
		block.accept(visitor);
		
		headers.add(StdArray.HEADER_NAME);
	}

	private IASTDeclarationStatement getOldDeclarationStatementIfExistent(IASTSimpleDeclaration oldDeclaration) {
		IASTNode parent = oldDeclaration.getParent();
		if (parent instanceof IASTDeclarationStatement)
			return (IASTDeclarationStatement) parent;
		return null;
	}

	private IASTExpression getArrayCountFromCopyAsExpression(IASTArrayModifier modifier, IASTDeclarator declarator) {
		IASTExpression constantExpression = modifier.getConstantExpression(); // assumes to be copied with location info
		if(LiteralAnalyzer.isInteger(constantExpression)) {
			return constantExpression;
		}
		else {
			// we need to create a new constant expression
			ICPPASTInitializerList oldInitializerList = (ICPPASTInitializerList)declarator.getInitializer().getChildren()[0];
			String dimensionFromInitializer = String.valueOf(oldInitializerList.getClauses().length);
			return ExtendedNodeFactory.newLiteralExpression(dimensionFromInitializer);
		}
	}


	
	private IASTSimpleDeclaration newRefactoredSimpleDeclarationFromDeclarator(IASTDeclarator declarator) {
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)declarator.getParent();
		// clone it and construct new declaration it
		IASTDeclarator copiedDeclarator = declarator.copy(CopyStyle.withLocations);
		IASTDeclSpecifier valuetypedeclspec = declaration.getDeclSpecifier().copy(CopyStyle.withLocations);
		int storageclass = valuetypedeclspec.getStorageClass();
		valuetypedeclspec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		
		IASTSimpleDeclaration newSimpleDeclaration;
		IASTDeclarator newDeclarator;
		if (copiedDeclarator instanceof IASTArrayDeclarator){
			IASTArrayDeclarator arrayDeclarator = (IASTArrayDeclarator) copiedDeclarator;
			IASTDeclarator nested=arrayDeclarator.getNestedDeclarator();
			if (nested==null){
				nested=ExtendedNodeFactory.newDeclarator();
			}
			IASTPointerOperator[] pointerOperators = arrayDeclarator.getPointerOperators();
			for (IASTPointerOperator operator:pointerOperators){
				if (operator != null)
					nested.addPointerOperator(operator);
			}
			IASTArrayModifier[] modifiers = arrayDeclarator.getArrayModifiers();
			ICPPASTNamedTypeSpecifier newNamedTypeSpecifier=null;
			IASTTypeId newtypeid=ExtendedNodeFactory.newTypeId(valuetypedeclspec, nested);
			for (int i=modifiers.length-1;i>=0;--i){
				IASTArrayModifier modifier=modifiers[i];
				IASTExpression dimension=getArrayCountFromCopyAsExpression(modifier, arrayDeclarator);

				newNamedTypeSpecifier=makeStdArray(newtypeid,dimension);
				if (i>0){
					newtypeid=ExtendedNodeFactory.newIASTTypeId(newNamedTypeSpecifier);
				}
			}
			newSimpleDeclaration = ExtendedNodeFactory.newSimpleDeclaration(newNamedTypeSpecifier);
			newDeclarator = ExtendedNodeFactory.newDeclarator(arrayDeclarator.getName().toString());

		} else { 
			// no array declarator, just use the copy
			newSimpleDeclaration = ExtendedNodeFactory.newSimpleDeclaration(valuetypedeclspec);
			newDeclarator = copiedDeclarator;
		}
		IASTInitializerClause initializerClause = DeclaratorAnalyzer.getInitializerClause(declarator);
		if(initializerClause != null) {
			initializerClause = initializerClause.copy(CopyStyle.withLocations);
			if (newDeclarator == copiedDeclarator){ // non-array case
				newDeclarator.setInitializer(ExtendedNodeFactory.newEqualsInitializer(initializerClause));
			} else { // array, thus add braces
				newDeclarator.setInitializer(ExtendedNodeFactory.newEqualsInitializerWithList(initializerClause));
			}
		}

		newSimpleDeclaration.addDeclarator(newDeclarator);
		newSimpleDeclaration.getDeclSpecifier().setStorageClass(storageclass); // re-establish original storage class
		return newSimpleDeclaration;
	}

	
	private ICPPASTNamedTypeSpecifier makeStdArray(IASTTypeId newtypeid, IASTExpression dimension) {
		// newnamedspecifier=qualifiedname(name("std"),templateid(name("array"),newtypeid,dimension))
		ICPPASTName std = ExtendedNodeFactory.newName(StdArray.STD);
		ICPPASTName array = ExtendedNodeFactory.newName(StdArray.ARRAY);
		ICPPASTTemplateId templateid = ExtendedNodeFactory.newTemplateId(array);
		templateid.addTemplateArgument(newtypeid);
		templateid.addTemplateArgument(dimension);
		ICPPASTQualifiedName qname = ExtendedNodeFactory.newQualifiedName(std);
		qname.addName(templateid);
		return ExtendedNodeFactory.newTypedefNameSpecifier(qname);
	}
}
