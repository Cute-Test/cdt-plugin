/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;


/**
 * @author Emanuel Graf
 *
 */
public class NewTestFunctionAction extends AbstractFunctionAction{
	
	protected static final String TEST_STMT = "\tASSERTM(\"start writing tests\", false);";

	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException {
		MultiTextEdit mEdit = new MultiTextEdit();
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;

			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				int insertFileOffset = getInsertOffset(astTu, selection);

				SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
				astTu.accept(suitPushBackFinder);
				
				mEdit.addChild(createdEdit(insertFileOffset, doc, funcName));

				//FIXME this check for existing pushback might have unwanted side effect, breaking linkmodel
				//if(!checkNameExist(astTu,funcName,suitPushBackFinder))
				mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
						funcName, suitPushBackFinder));
			}
		}
		return mEdit;
	}

	//adding the new test function
	private TextEdit createdEdit(int insertTestFuncFileOffset, IDocument doc, String funcName) {
		String newLine = TextUtilities.getDefaultLineDelimiter(doc);
		StringBuilder builder = new StringBuilder();
		builder.append("void ");
		builder.append(funcName);
		builder.append("(){");
		builder.append(newLine);
		builder.append(TEST_STMT);
		builder.append(newLine);
		builder.append("}");
		builder.append(newLine);
		builder.append(newLine);
		TextEdit iedit = new InsertEdit(insertTestFuncFileOffset, builder.toString());
		return iedit;
	}
}