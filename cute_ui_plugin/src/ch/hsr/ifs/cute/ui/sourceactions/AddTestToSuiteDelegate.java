package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class AddTestToSuiteDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart editor;
	private AddTestToSuite functionAction;

	public AddTestToSuiteDelegate(){
		functionAction = new AddTestToSuite();
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}
	public void dispose() {}
	public void init(IWorkbenchWindow window) {}
	public void selectionChanged(IAction action, ISelection selection) {}

	public void run(IAction action) {

		try {
			IEditorPart editor = getEditor();

			saveEditor(editor);

			if(editor == null)return;

			TextEditor ceditor = (TextEditor) editor;
			IEditorInput editorInput = ceditor.getEditorInput();
			IDocumentProvider prov = ceditor.getDocumentProvider();
			IDocument doc = prov.getDocument(editorInput);

			MultiTextEdit mEdit;
			ISelection sel = ceditor.getSelectionProvider().getSelection();
			mEdit = functionAction.createEdit(ceditor, editorInput, doc, sel);

			RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(doc, mEdit, TextEdit.CREATE_UNDO);
			processor.performEdits();
			saveEditor(editor);
			this.editor = null;
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	private void saveEditor(IEditorPart editor) {
		if (editor != null && editor instanceof TextEditor) {
			if(editor.isDirty()){
				editor.doSave(new NullProgressMonitor());
			}
		}
	}

	private IEditorPart getEditor() {
		if(editor == null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			 if (page.isEditorAreaVisible()
			      && page.getActiveEditor() != null
			      && page.getActiveEditor() instanceof TextEditor) {
			         editor = page.getActiveEditor();
			 }
		}
		return editor;
	}
	
}
