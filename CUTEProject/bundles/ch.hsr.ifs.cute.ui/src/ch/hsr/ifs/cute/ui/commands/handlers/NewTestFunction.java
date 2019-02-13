package ch.hsr.ifs.cute.ui.commands.handlers;

import java.util.Optional;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.sourceactions.AddNewTestRefactoring;


public class NewTestFunction extends AbstractHandler {

    private static final String DEFAULT_FUNCTION_NAME = "newTestFunction";

    /**
     * This is an introspection points for the unit tests. leave it as is!
     */
    private static EditorLinkedModeUI linkedModeUI;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        getEditor(event).ifPresent(e -> {
            IFileEditorInput input = e.getEditorInput().getAdapter(IFileEditorInput.class);
            IDocument document = e.getDocumentProvider().getDocument(input);
            ISelection selection = e.getSelectionProvider().getSelection();
            if (input != null) {
                createNewTest(e, input, document, selection);
            }
        });

        return null;
    }

    static private void createNewTest(TextEditor e, IFileEditorInput input, IDocument document, ISelection selection) {
        AddNewTestRefactoring refactoring = new AddNewTestRefactoring(DEFAULT_FUNCTION_NAME);
        try {
            MultiTextEdit edit = refactoring.createEdit(input.getFile(), document, selection);
            RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(document, edit, TextEdit.CREATE_UNDO);
            processor.performEdits();
            updateLinkedMode(e, document, edit);
        } catch (CoreException | MalformedTreeException | BadLocationException err) {
            CuteCorePlugin.log(err);
        }
    }

    static private Optional<TextEditor> getEditor(ExecutionEvent event) {
        if (event != null) {
            IEditorPart eventEditor = HandlerUtil.getActiveEditor(event);
            if (eventEditor instanceof TextEditor) {
                return Optional.of((TextEditor) eventEditor);
            }
        }

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page.isEditorAreaVisible() && page.getActiveEditor() instanceof TextEditor) {
            return Optional.of((TextEditor) page.getActiveEditor());
        }

        return Optional.empty();
    }

    static private void updateLinkedMode(TextEditor editor, IDocument doc, MultiTextEdit mEdit) throws BadLocationException {
        ISourceViewer viewer = ((CEditor) editor).getViewer();
        LinkedModeModel model = new LinkedModeModel();

        LinkedPositionGroup group = new LinkedPositionGroup();

        /* linking the name together (which will change together)for the very 1st edit, subsequent changes would need refactoring:rename */
        TextEdit[] edits = mEdit.getChildren();
        int totalEditLength = 0;
        for (TextEdit textEdit : edits) {
            String insert = ((InsertEdit) textEdit).getText();
            if (insert.contains(DEFAULT_FUNCTION_NAME)) {
                int start = textEdit.getOffset();
                int indexOfFuncName = insert.indexOf(DEFAULT_FUNCTION_NAME);
                group.addPosition(new LinkedPosition(viewer.getDocument(), start + indexOfFuncName + totalEditLength, DEFAULT_FUNCTION_NAME
                        .length()));
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

    private static int getCursorEndPosition(TextEdit[] edits, String newLine) {
        int result = edits[0].getOffset() + edits[0].getLength();
        int leadingEditsLengthSum = 0;
        for (TextEdit textEdit : edits) {
            String insert = ((InsertEdit) textEdit).getText();
            if (insert.contains(AddNewTestRefactoring.TEST_STMT.trim())) {
                result = (leadingEditsLengthSum + textEdit.getOffset() + insert.indexOf(AddNewTestRefactoring.TEST_STMT.trim()));
                break;
            } else {
                leadingEditsLengthSum += ((InsertEdit) textEdit).getLength();
            }
        }
        return result;
    }

    private static int getExitPositionLength() {
        return AddNewTestRefactoring.TEST_STMT.trim().length();
    }
}
