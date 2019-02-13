package ch.hsr.ifs.cute.ui.commands.handlers;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.sourceactions.AddTestToSuiteRefactoring;


public class AddTestToSuite extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        getEditor(event).ifPresent(e -> {
            IFileEditorInput input = e.getEditorInput().getAdapter(IFileEditorInput.class);
            IDocument document = e.getDocumentProvider().getDocument(input);
            ISelection selection = e.getSelectionProvider().getSelection();
            if (input != null) {
                addTestToSuite(input, document, selection);
            }
        });
        return null;
    }

    static private void addTestToSuite(IFileEditorInput input, IDocument document, ISelection selection) {
        try {
            final AddTestToSuiteRefactoring refactoring = new AddTestToSuiteRefactoring();
            MultiTextEdit edit = refactoring.createEdit(input.getFile(), document, selection);
            final RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(document, edit, TextEdit.CREATE_UNDO);
            processor.performEdits();
        } catch (MalformedTreeException | BadLocationException | CoreException e) {
            CuteCorePlugin.log(e);
        }
    }

    static private Optional<TextEditor> getEditor(ExecutionEvent event) {
        IEditorPart eventEditor = HandlerUtil.getActiveEditor(event);
        if (eventEditor instanceof TextEditor) {
            return Optional.of((TextEditor) eventEditor);
        }

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page.isEditorAreaVisible() && page.getActiveEditor() instanceof TextEditor) {
            return Optional.of((TextEditor) page.getActiveEditor());
        }

        return Optional.empty();
    }
}
