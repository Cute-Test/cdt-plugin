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
package ch.hsr.ifs.cute.ui.checkers;

import org.eclipse.cdt.codan.ui.AbstarctCodanCMarkerResolution;
import org.eclipse.cdt.codan.ui.CodanEditorUtility;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.sourceactions.AddTestToSuite;

/**
 * @author Emanuel Graf IFS
 *
 */
public class UnregisteredTestResolution extends AbstarctCodanCMarkerResolution {

	public UnregisteredTestResolution() {
	}


	public String getLabel() {
		return "Add test to suite";
	}

	

	public void run(IMarker marker) {
		IEditorPart editorPart;
        try {
	        editorPart = CodanEditorUtility.openInEditor(marker);
        } catch (PartInitException e) {
	        UiPlugin.log(e);
	        return;
        }
		if (editorPart instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			int offset = getOffset(marker, doc);
			TextSelection sel = new TextSelection(offset + 3, 3);
			AddTestToSuite action = new AddTestToSuite();
			try {
				MultiTextEdit edit = action.createEdit(editor, editor.getEditorInput(), doc, sel);
				RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(doc, edit, TextEdit.CREATE_UNDO);
				processor.performEdits();
				if(editor.isDirty()){
					editor.doSave(new NullProgressMonitor());
				}
			} catch (CoreException e) {
				UiPlugin.log(e);
			} catch (MalformedTreeException e) {
				UiPlugin.log(e);
			} catch (BadLocationException e) {
				UiPlugin.log(e);
			}
		}
	}


	@Override
	public void apply(IMarker marker, IDocument document) {}


}
