/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import ch.hsr.ifs.cute.ui.ASTUtil;

/**
 * @since 4.0
 */
public class AddTestToSuite extends AbstractFunctionAction {

	@Override
	public MultiTextEdit createEdit(ITextEditor ceditor, IEditorInput editorInput, IDocument doc, ISelection sel) throws CoreException {
		AddStrategy adder = new NullStrategy(doc);
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;
			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);

				NodeAtCursorFinder n = new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
				IASTFunctionDefinition def = getFunctionDefinition(n.getNode());

				if (def == null) {
					def = getFunctionDefIfIsFunctor(n.getNode());
				}
				if (ASTUtil.isTestFunction(def)) {
					if (def != null && isMemberFunction(def)) { //In .cpp file
						SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
						astTu.accept(suitPushBackFinder);
						IASTName name = def.getDeclarator().getName();
						if (name instanceof ICPPASTOperatorName && name.toString().contains("()")) { //$NON-NLS-1$
							adder = new AddFunctorToSuiteStrategy(doc, astTu, n.getNode(), editorFile);
						} else {
							adder = new AddMemberFunctionStrategy(doc, editorFile, astTu, name, suitPushBackFinder);
						}
					} else if (def != null && isFunction(def)) {
						SuitePushBackFinder finder = new SuitePushBackFinder();
						astTu.accept(finder);
						adder = new AddFunctionToSuiteStrategy(doc, editorFile, astTu, def.getDeclarator().getName(), finder);
					}
				}
			}
		}
		return adder.getEdit();
	}

	protected IASTFunctionDefinition getFunctionDefIfIsFunctor(IASTNode n) {
		if (n instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) n;
			if (sDecl.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
				IASTCompositeTypeSpecifier comDeclSpec = (IASTCompositeTypeSpecifier) sDecl.getDeclSpecifier();
				IASTDeclaration[] members = comDeclSpec.getMembers();
				for (IASTDeclaration iastDeclaration : members) {
					if (iastDeclaration instanceof IASTFunctionDefinition) {
						IASTFunctionDefinition funcDef = (IASTFunctionDefinition) iastDeclaration;
						IASTName funcName = funcDef.getDeclarator().getName();
						if (funcName instanceof ICPPASTOperatorName && funcName.toString().contains("()")) { //$NON-NLS-1$
							return funcDef;
						}

					}
				}
			}
		}
		return n != null && n.getParent() != null ? getFunctionDefIfIsFunctor(n.getParent()) : null;
	}

	private boolean isFunction(IASTFunctionDefinition def) {
		return def.getDeclarator().getName().resolveBinding() instanceof ICPPFunction;
	}

	private boolean isMemberFunction(IASTFunctionDefinition def) {
		return def.getDeclarator().getName().resolveBinding() instanceof ICPPMethod;
	}

	private IASTFunctionDefinition getFunctionDefinition(IASTNode node) {
		if (node == null)
			return null;
		if (node instanceof IASTFunctionDefinition) {
			return (IASTFunctionDefinition) node;
		} else {
			IASTNode parent = node.getParent();
			if (parent != null) {
				return getFunctionDefinition(parent);
			} else {
				return null;
			}
		}
	}

}