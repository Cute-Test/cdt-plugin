/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Emanuel Graf IFS
 *
 */
public class AddMemberFunctionToSuite extends AbstractFunctionAction {

	@Override
	public MultiTextEdit createEdit(TextEditor ceditor, IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException {
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;
			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				
				NodeAtCursorFinder n= new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
				IASTFunctionDefinition def = getFunctenDefinition(n.getNode());
				if(def != null && isMemberFunction(def) && def.getDeclarator().getName() instanceof ICPPASTQualifiedName) { //In .cpp file
					SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
					astTu.accept(suitPushBackFinder);
					StringBuilder builder = getString((ICPPASTQualifiedName) def.getDeclarator().getName(), suitPushBackFinder);
					MultiTextEdit edit = new MultiTextEdit();
					edit.addChild(createPushBackEdit(editorFile, astTu, suitPushBackFinder, builder));
					return edit;
				}
			}
		}
		return null;
	}

	private boolean isMemberFunction(IASTFunctionDefinition def) {
		return def.getDeclarator().getName().resolveBinding() instanceof ICPPMethod;
	}

	private StringBuilder getString(ICPPASTQualifiedName qName, SuitePushBackFinder suitPushBackFinder) {
		StringBuilder sb = new StringBuilder(newLine + "\t"); // s.push_back(CUTE_SMEMFUN(TestClass,test2);");
		sb.append(suitPushBackFinder.getSuiteDeclName().toString());
		sb.append(".push_back(CUTE_SMEMFUN(");
		IASTName[] names = qName.getNames();
		if (names.length > 2) {
			sb.append(names[names.length -2]);
			sb.append(", ");
			sb.append(names[names.length-1]);
			sb.append("));");
		}
		return sb;
	}

	private IASTFunctionDefinition getFunctenDefinition(IASTNode node) {
		if (node instanceof IASTFunctionDefinition) {
			return (IASTFunctionDefinition) node;
		}else {
			IASTNode parent = node.getParent();
			if(parent != null) {
				return getFunctenDefinition(parent);
			}else {
				return null;
			}
		}
	}

}
