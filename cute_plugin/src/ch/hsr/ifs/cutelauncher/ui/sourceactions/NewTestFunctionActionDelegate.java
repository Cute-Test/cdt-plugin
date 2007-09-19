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

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * @author Emanuel Graf
 *
 */
public class NewTestFunctionActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart editor;
	
	public void dispose() {
		// TODO Auto-generated method stub
		
	}


	public void run(IAction action) {
		try {	
			if (editor != null && editor instanceof TextEditor) {
				NewTestFunctionAction newFuncAction = new NewTestFunctionAction();
				TextEditor ceditor = (TextEditor) editor;
				IEditorInput editorInput = ceditor.getEditorInput();
				IDocumentProvider prov = ceditor.getDocumentProvider();
				IDocument doc = prov.getDocument(editorInput);
				String newLine = TextUtilities.getDefaultLineDelimiter(doc);
				String funcName = "newTestFunction";
				
				MultiTextEdit mEdit = newFuncAction.createEdit(ceditor, editorInput, doc, funcName);
				mEdit.apply(doc);
				
				ISourceViewer viewer = ((CEditor)editor).getViewer();
				LinkedModeModel model = new LinkedModeModel();
				
				LinkedPositionGroup group = new LinkedPositionGroup();
				
				TextEdit[] edits = mEdit.getChildren();
				for (TextEdit textEdit : edits) {
					String insert = ((InsertEdit)textEdit).getText();
					if(insert.contains(funcName)) {
						int start = textEdit.getOffset();
						int indexOfFuncName = insert.indexOf(funcName);
						group.addPosition(new LinkedPosition(viewer.getDocument(), start + indexOfFuncName, funcName.length()));
					}
				}
				
				model.addGroup(group);
				model.forceInstall();
				
				
				LinkedModeUI linkedModeUI = new EditorLinkedModeUI(model, viewer);
				linkedModeUI.setExitPosition(viewer, getCursorEndPosition(edits, newLine), NewTestFunctionAction.TEST_STMT.trim().length(), Integer.MAX_VALUE);
				linkedModeUI.setCyclingMode(LinkedModeUI.CYCLE_ALWAYS);
				linkedModeUI.enter();
				
			}
			System.err.println("error");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		} 
		
	}


	

	private int getCursorEndPosition(TextEdit[] edits, String newLine) {
		for (TextEdit textEdit : edits) {
			String insert = ((InsertEdit)textEdit).getText();
			if(insert.contains(NewTestFunctionAction.TEST_STMT.trim())) {
				return (textEdit.getOffset() + insert.indexOf(NewTestFunctionAction.TEST_STMT.trim()));
			}
		}
		return edits[0].getOffset() + edits[0].getLength();
	}


	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}


	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}
	
	

}
