/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createvariable;

import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBaseDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;

public class CreateLocalVariableRefactoring extends CRefactoring3 {

	private final String missingName;

	public CreateLocalVariableRefactoring(ISelection selection, String missingName) {
		super(selection);
		this.missingName = missingName;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {
		IASTTranslationUnit localunit = refactoringContext.getAST(tu, pm);
		IASTNode selectedNode = localunit.getNodeSelector(null).findEnclosingName(getSelection().getOffset(), getSelection().getLength());
		if (selectedNode == null) {
			selectedNode = localunit.getNodeSelector(null).findName(getSelection().getOffset(), getSelection().getLength());
			assert (false);
		}
		IASTIdExpression owner = TddHelper.getAncestorOfType(selectedNode, IASTIdExpression.class);
		IASTName variableName = new CPPASTName(missingName.toCharArray());

		CPPASTBaseDeclSpecifier spec = CreateLocalVariableRefactoring.calculateType(localunit, getSelection());
		CPPASTSimpleDeclaration simpledec = new CPPASTSimpleDeclaration(spec);
		simpledec.addDeclarator(new CPPASTDeclarator(variableName));

		CPPASTDeclarationStatement decl = new CPPASTDeclarationStatement(simpledec);
		IASTFunctionDefinition outerFunction = TddHelper.getOuterFunctionDeclaration(localunit, getSelection());

		IASTNode insertionPoint = getInsertionPointFromMacro(owner);
		if (insertionPoint == null) {
			insertionPoint = getSimpleInsertionPoint(owner);
		}

		ASTRewrite rewrite = collector.rewriterForTranslationUnit(localunit);
		rewrite.insertBefore(outerFunction.getBody(), insertionPoint, decl, null);
	}

	private IASTNode getSimpleInsertionPoint(IASTIdExpression owner) {
		IASTNode result;
		result = TddHelper.getAncestorOfType(owner, IASTExpressionStatement.class);
		if (result == null) {
			result = TddHelper.getAncestorOfType(owner, CPPASTDeclarationStatement.class);
		}
		return result;
	}

	private IASTNode getInsertionPointFromMacro(IASTIdExpression owner) {
		return TddHelper.getAncestorOfType(owner, CPPASTIfStatement.class);
	}

	public static CPPASTBaseDeclSpecifier calculateType(IASTTranslationUnit localunit, TextSelection selection) {
		CPPASTBaseDeclSpecifier type = TypeHelper.findTypeInAst(localunit, selection);
		if (type == null) {
			return TypeHelper.getDefaultType();
		}
		return type;
	}
}
