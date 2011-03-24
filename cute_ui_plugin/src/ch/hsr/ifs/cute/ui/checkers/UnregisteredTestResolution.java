/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.checkers;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
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
public class UnregisteredTestResolution extends AbstractCodanCMarkerResolution {

	public UnregisteredTestResolution() {
	}


	public String getLabel() {
		return Messages.UnregisteredTestResolution_0;
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
