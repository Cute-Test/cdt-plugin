/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import ch.hsr.ifs.cute.ui.CuteUIPlugin;


/**
 * @author Emanuel Graf
 * @since 4.0
 *
 */
public class NewTestFunctionActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {

    protected IEditorPart                 editor;
    protected LinkedModeUI                linkedModeUI;
    protected final String                funcName;      // used for linking during 1st edit
    protected final NewTestFunctionAction functionAction;

    public NewTestFunctionActionDelegate() {
        this.funcName = "newTestFunction";
        this.functionAction = new NewTestFunctionAction("newTestFunction");
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
    }

    @Override
    public void dispose() {}

    @Override
    public void init(IWorkbenchWindow window) {}

    @Override
    public void selectionChanged(IAction action, ISelection selection) {}

    protected boolean isCorrectEditor() {
        if (editor == null) {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page.isEditorAreaVisible() && page.getActiveEditor() != null && page.getActiveEditor() instanceof TextEditor) {
                editor = page.getActiveEditor();
            }
        }
        if (editor != null && editor instanceof TextEditor) {
            if (editor.isDirty()) {
                editor.doSave(new NullProgressMonitor());
            }
            return true;
        }
        return false;
    }

    @Override
    public void run(IAction action) {
        try {
            if (!isCorrectEditor()) return;

            TextEditor ceditor = (TextEditor) editor;
            IEditorInput editorInput = ceditor.getEditorInput();
            IDocumentProvider prov = ceditor.getDocumentProvider();
            IDocument doc = prov.getDocument(editorInput);
            ISelection sel = ceditor.getSelectionProvider().getSelection();
            IFileEditorInput fei = editorInput.getAdapter(IFileEditorInput.class);
            if (fei != null) {
                MultiTextEdit mEdit = functionAction.createEdit(fei.getFile(), doc, sel);

                RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(doc, mEdit, TextEdit.CREATE_UNDO);
                processor.performEdits();

                updateLinkedMode(doc, mEdit);
            }
        } catch (CoreException e) {
            CuteUIPlugin.log("Exception while running new test function action", e);
        } catch (MalformedTreeException e) {
            CuteUIPlugin.log("Exception while running new test function action", e);
        } catch (BadLocationException e) {
            CuteUIPlugin.log("Exception while running new test function action", e);
        } finally {
            editor = null;
        }
    }

    private void updateLinkedMode(IDocument doc, MultiTextEdit mEdit) throws BadLocationException {
        ISourceViewer viewer = ((CEditor) editor).getViewer();
        LinkedModeModel model = new LinkedModeModel();

        LinkedPositionGroup group = new LinkedPositionGroup();

        /* linking the name together (which will change together)for the very 1st edit, subsequent changes would need refactoring:rename */
        TextEdit[] edits = mEdit.getChildren();
        int totalEditLength = 0;
        for (TextEdit textEdit : edits) {
            String insert = ((InsertEdit) textEdit).getText();
            if (insert.contains(funcName)) {
                int start = textEdit.getOffset();
                int indexOfFuncName = insert.indexOf(funcName);
                group.addPosition(new LinkedPosition(viewer.getDocument(), start + indexOfFuncName + totalEditLength, funcName.length()));
                totalEditLength += insert.length();
            }
        }

        if (!group.isEmpty()) {
            model.addGroup(group);
            model.forceInstall();

            /* after pressing enter of 1st edit, for newTestfunction select "assert" line from start to end of it */
            String newLine = TextUtilities.getDefaultLineDelimiter(doc);
            linkedModeUI = new EditorLinkedModeUI(model, viewer);
            linkedModeUI.setExitPosition(viewer, getCursorEndPosition(edits, newLine), getExitPositionLength(), Integer.MAX_VALUE);
            linkedModeUI.setCyclingMode(LinkedModeUI.CYCLE_ALWAYS);
            linkedModeUI.enter();
        }
    }

    int getCursorEndPosition(TextEdit[] edits, String newLine) {
        int result = edits[0].getOffset() + edits[0].getLength();
        int leadingEditsLengthSum = 0;
        for (TextEdit textEdit : edits) {
            String insert = ((InsertEdit) textEdit).getText();
            if (insert.contains(NewTestFunctionAction.TEST_STMT.trim())) {
                result = (leadingEditsLengthSum + textEdit.getOffset() + insert.indexOf(NewTestFunctionAction.TEST_STMT.trim()));
                break;
            } else {
                leadingEditsLengthSum += ((InsertEdit) textEdit).getLength();
            }
        }
        return result;
    }

    int getExitPositionLength() {
        return NewTestFunctionAction.TEST_STMT.trim().length();
    }

    public LinkedModeUI testOnlyGetLinkedMode() {
        return linkedModeUI;
    }

}