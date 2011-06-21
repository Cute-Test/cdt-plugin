/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createvariable;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;
import ch.hsr.ifs.cute.tdd.createfunction.FunctionCreationHelper;

@SuppressWarnings("restriction")
public class CreateMemberVariableRefactoring extends CRefactoring3 {

	private boolean priv;
	private boolean isArray;
	private IASTInitializerClause initClause;

	public CreateMemberVariableRefactoring(ISelection selection, CodanArguments ca, RefactoringASTCache astCache) {
		super(selection, astCache);
	}

	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) 
			throws CoreException, OperationCanceledException {
		IASTTranslationUnit localunit = astCache.getAST(tu, pm);
		IASTName selectedNode = FunctionCreationHelper.getMostCloseSelectedNodeName(localunit, getSelection());
		ICPPASTCompositeTypeSpecifier type = TypeHelper.getTargetTypeOfField(localunit, selectedNode, astCache);
		IASTDeclaration newMember = getMemberVariableDeclaration(selectedNode, type);
		if (priv) {
			TddHelper.writePrivateDefinitionTo(collector, type, newMember);
		} else {
			TddHelper.writeDefinitionTo(collector, type, newMember);
		}
		setLinkedModeInformation(localunit, type, newMember);
	}

	private IASTDeclaration getMemberVariableDeclaration(IASTName variableName, ICPPASTCompositeTypeSpecifier type) {
		IASTDeclSpecifier declspec = getDeclSpec(variableName);
		CPPASTSimpleDeclaration newDeclaration = new CPPASTSimpleDeclaration(declspec);
		CPPASTDeclarator newDeclarator;
		if (isArray) {
			assert(initClause instanceof IASTInitializerList);
			IASTExpression size = new CPPASTLiteralExpression(ICPPASTLiteralExpression.lk_integer_constant, (((IASTInitializerList) initClause).getSize() + "").toCharArray()); //$NON-NLS-1$
			CPPASTArrayDeclarator array = new CPPASTArrayDeclarator(variableName.copy());
			array.addArrayModifier(new CPPASTArrayModifier(size));
			newDeclarator = array;
		} else {
			newDeclarator = new CPPASTDeclarator(variableName.copy());
		}
		if (isVoid(declspec)) {
			newDeclarator.addPointerOperator(new CPPASTPointer());
		}
		newDeclaration.addDeclarator(newDeclarator);
		newDeclaration.setParent(type);
		return newDeclaration;
	}

	private boolean isVoid(IASTDeclSpecifier declspec) {
		return declspec instanceof IASTSimpleDeclSpecifier && ((IASTSimpleDeclSpecifier)declspec).getType() == ICPPASTSimpleDeclSpecifier.t_void;
	}

	private IASTDeclSpecifier getDeclSpec(IASTName varName) {
		priv = true;
		if (varName.getParent() instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference ref = (ICPPASTFieldReference)varName.getParent();
			if (hasThisAsLValue(ref)) {
				ICPPASTBinaryExpression ex = ToggleNodeHelper.getAncestorOfType(ref, ICPPASTBinaryExpression.class);
				if (ex == null) {
					return createVoidDeclSpec();
				}
				IASTExpression rSide = ex.getOperand2();
				return getDeclSpecOfType(rSide);
			}
			if (ref.getParent() instanceof ICPPASTBinaryExpression) { 
				IASTInitializerClause rSide = ((ICPPASTBinaryExpression)ref.getParent()).getInitOperand2();
				return getDeclSpecOfType(rSide);
			}
			if (isThisKeyword((ICPPASTFieldReference)varName.getParent())) {
				return createVoidDeclSpec();
			}
			IASTSimpleDeclaration decl = ToggleNodeHelper.getAncestorOfType(varName, IASTSimpleDeclaration.class);
			if (decl != null) {
				priv = false;
				return decl.getDeclSpecifier().copy();
			}
		} else if (varName.getParent() instanceof ICPPASTConstructorChainInitializer) {
			ICPPASTConstructorChainInitializer chainInitializer = (ICPPASTConstructorChainInitializer) varName.getParent();
			IASTInitializerClause firstClause = getInitializerClause(chainInitializer);
			if (firstClause != null) {
				return getDeclSpecOfType(firstClause);
			}
		}
		// any other cases? e.g. type->member or (*type).member, etc...
		priv = false;
		return  createVoidDeclSpec();
	}

	private boolean hasThisAsLValue(ICPPASTFieldReference ref) {
		IASTExpression owner = ref.getFieldOwner();
		if (ref.getFieldOwner() instanceof ICPPASTLiteralExpression) {
			if (((ICPPASTLiteralExpression)owner).getKind() == ICPPASTLiteralExpression.lk_this) {
				return true;
			}
		}
		return false;
	}

	private boolean isThisKeyword(ICPPASTFieldReference fieldRef) {
		return fieldRef.getFieldOwner() instanceof ICPPASTLiteralExpression
				&& ((ICPPASTLiteralExpression) fieldRef.getFieldOwner())
						.getKind() == ICPPASTLiteralExpression.lk_this;
	}

	private IASTInitializerClause getInitializerClause(ICPPASTConstructorChainInitializer chain) {
		if (!(chain.getInitializer() instanceof ICPPASTConstructorInitializer)) {
			return null;
		}
		ICPPASTConstructorInitializer init = (ICPPASTConstructorInitializer) chain.getInitializer();
		IASTInitializerClause[] args = init.getArguments();
		// Handle constructors with multiple arguments too
		if (args.length > 0) {
			return args[0];
		}
		return null;
	}

	private IASTDeclSpecifier getDeclSpecOfType(IASTInitializerClause clause) {
		if (TypeHelper.isString(clause)) {
			return createStringDeclSpec();
		}
		if (clause instanceof IASTInitializerList) {
			isArray = true;
			initClause = clause;
		}
		IType type = TypeHelper.getTypeOf(clause);
		type = TypeHelper.windDownToRealType(type, true);
		return TypeHelper.getDeclarationSpecifierOfType(type);
	}

	private CPPASTNamedTypeSpecifier createStringDeclSpec() {
		return new CPPASTNamedTypeSpecifier(new CPPASTName("std::string".toCharArray())); //$NON-NLS-1$
	}

	public static IASTDeclSpecifier createVoidDeclSpec() {
		IASTSimpleDeclSpecifier simple = new CPPASTSimpleDeclSpecifier();
		simple.setType(Kind.eVoid);
		return simple;
	}
}
