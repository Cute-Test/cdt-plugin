package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
public class AddTestFunctiontoSuiteDelegate extends AbstractFunctionActionDelegate {
	
	public AddTestFunctiontoSuiteDelegate(){
		super("AddTestFunctiontoSuite",null);
	}
	
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		System.out.println("set editor");
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		System.out.println("run");
		/*MessageConsole console = new MessageConsole("My Console", null);
		console.activate();
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
		MessageConsoleStream stream = console.newMessageStream();
		stream.println("Hello, world!");*/
		
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		System.out.println("selection change");
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		System.out.println("init");
	}

	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine){
		return 0;
	}
	@Override
	int getExitPositionLength(){
		return 0;
	}
}/*set editor
selection change
run*/

class AddTestFunctiontoSuiteAction extends AbstractFunctionAction{
	//go up ast 
	//find surrounding body and then mark name
	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException{
		MultiTextEdit mEdit = new MultiTextEdit();
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;

			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				int insertFileOffset = getInsertOffset(astTu, selection);
				/*SuitPushBackFinder suitPushBackFinder = new SuitPushBackFinder();
				astTu.accept(suitPushBackFinder);

				mEdit.addChild(createdEdit(insertFileOffset, doc, funcName));
				mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
						funcName, suitPushBackFinder));*/

			}
		}
		return mEdit;
	}
}