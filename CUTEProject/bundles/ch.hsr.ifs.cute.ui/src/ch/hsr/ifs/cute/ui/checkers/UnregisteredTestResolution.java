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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.sourceactions.AbstractFunctionAction;
import ch.hsr.ifs.cute.ui.sourceactions.AddTestToSuiteRefactoring;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 *
 */
public class UnregisteredTestResolution extends AbstractCodanCMarkerResolution {

    public UnregisteredTestResolution() {}

    @Override
    public String getLabel() {
        return CheckerMessages.UnregisteredTest;
    }

    @Override
    public void apply(IMarker marker, IDocument document) {
        try {
            final IResource resource = marker.getResource();
            if (resource != null) {
                IFile file = resource.getAdapter(IFile.class);
                if (file != null) {
                    performChange(marker, document, file);
                }
            }
        } catch (CoreException e) {
            CuteUIPlugin.log(e);
        } catch (MalformedTreeException e) {
            CuteUIPlugin.log(e);
        } catch (BadLocationException e) {
            CuteUIPlugin.log(e);
        }
    }

    private void performChange(IMarker marker, IDocument document, IFile file) throws CoreException, BadLocationException {
        TextSelection sel = getNonWhitespaceOffset(marker, document);

        AbstractFunctionAction action = new AddTestToSuiteRefactoring();
        MultiTextEdit edit = action.createEdit(file, document, sel);
        RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(document, edit, TextEdit.CREATE_UNDO);
        processor.performEdits();
    }

    private TextSelection getNonWhitespaceOffset(IMarker marker, IDocument document) throws BadLocationException {
        //the following text offset magic will not be necessary anymore once markers are added to actual nodes and not to the full line. (see ticket #
        IRegion lineInfo = document.getLineInformationOfOffset(getOffset(marker, document));
        String line = document.get(lineInfo.getOffset(), lineInfo.getLength()) + "x"; //do this so when calling trim() below, no whitespace gets trimmed at the end (if any)
        int offset = lineInfo.getOffset() + (line.length() - line.trim().length());
        TextSelection sel = new TextSelection(offset, 0);
        return sel;
    }
}
