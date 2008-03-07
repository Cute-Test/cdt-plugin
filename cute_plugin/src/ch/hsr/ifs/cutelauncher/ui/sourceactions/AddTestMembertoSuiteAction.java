package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

public class AddTestMembertoSuiteAction extends AbstractFunctionAction {

	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException {
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;
			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				
				NodeAtCursorFinder n= new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
		
				FunctionFinder ff=new FunctionFinder();
				astTu.accept(ff);
				for(Object i:ff.getClassStruct()){
					//stream.println(ff.getSimpleDeclarationNodeName((IASTSimpleDeclaration)(i))+"");
					if(((IASTNode)i).contains(n.getNode())){
						ArrayList<IASTDeclaration> publicMethods=ASTHelper.getPublicMethods((IASTSimpleDeclaration)i);
						System.out.print("");
						
					}
				}
				
		
			}
		}
		return new MultiTextEdit();
	}

}

// nested case of struct/class ?
