package ch.hsr.ifs.cutelauncher.ui.sourceactions;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public abstract class AbstractFunctionActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate{

	protected IEditorPart editor;
	protected final String funcName;//used for linking during 1st edit
	protected final AbstractFunctionAction functionAction;  
	protected AbstractFunctionActionDelegate(String funcName, AbstractFunctionAction functionAction){
		this.funcName=funcName;
		this.functionAction= functionAction;
	}
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}
	
	/*ensure texteditor is the active window, save previous changes first*/
	protected boolean isCorrectEditor(){
		if(editor == null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			 if (page.isEditorAreaVisible()
			      && page.getActiveEditor() != null
			      && page.getActiveEditor() instanceof TextEditor) {
			         editor = page.getActiveEditor();
			 }
		}
		if (editor != null && editor instanceof TextEditor) {
			if(editor.isDirty()){
				editor.doSave(new NullProgressMonitor());
			}
			return true;
		}
		return false;
	}
	
	//http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
	public void run(IAction action) {
		try {
			if(!isCorrectEditor())return;
			
			TextEditor ceditor = (TextEditor) editor;
			IEditorInput editorInput = ceditor.getEditorInput();
			IDocumentProvider prov = ceditor.getDocumentProvider();
			IDocument doc = prov.getDocument(editorInput);
			String newLine = TextUtilities.getDefaultLineDelimiter(doc);
			
			MultiTextEdit mEdit = functionAction.createEdit(ceditor, editorInput, doc, funcName);
			
			RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(doc, mEdit, TextEdit.CREATE_UNDO);
			processor.performEdits();
			
			ISourceViewer viewer = ((CEditor)editor).getViewer();				
			LinkedModeModel model = new LinkedModeModel();
			
			LinkedPositionGroup group = new LinkedPositionGroup();
			
			/*linking the name together for the 1st edit, subsequent changes would need refactoring:rename*/
			TextEdit[] edits = mEdit.getChildren();
			int totalEditLength = 0;
			for (TextEdit textEdit : edits) {
				String insert = ((InsertEdit)textEdit).getText();
				if(insert.contains(funcName)) {
					int start = textEdit.getOffset();
					int indexOfFuncName = insert.indexOf(funcName);
					group.addPosition(new LinkedPosition(viewer.getDocument(), start + indexOfFuncName + totalEditLength, funcName.length()));
					totalEditLength += insert.length();
				}
			}
			
			model.addGroup(group);
			model.forceInstall();
			
			/*after pressing enter of 1st edit, select "assert" line from start to end of it*/
			LinkedModeUI linkedModeUI = new EditorLinkedModeUI(model, viewer);
			linkedModeUI.setExitPosition(viewer, getCursorEndPosition(edits, newLine), getExitPositionLength(), Integer.MAX_VALUE);
			linkedModeUI.setCyclingMode(LinkedModeUI.CYCLE_ALWAYS);
			linkedModeUI.enter();

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

	abstract int getCursorEndPosition(TextEdit[] edits, String newLine);
	abstract int getExitPositionLength();
}