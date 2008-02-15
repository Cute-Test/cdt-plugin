package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
public class AddTestFunctortoSuiteDelegate extends
		AbstractFunctionActionDelegate {
	public AddTestFunctortoSuiteDelegate(){
		super("AddTestFunctortoSuite",new AddTestFunctortoSuiteAction());
	}
	
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int getExitPositionLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
class AddTestFunctortoSuiteAction extends AbstractFunctionAction{
	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException{
		if (editorInput instanceof FileEditorInput) {
			IFile editorFile = ((FileEditorInput) editorInput).getFile();
			IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
			scanforParenthesesOperator(astTu);
		}
		return null;
	}
	protected void scanforParenthesesOperator(IASTTranslationUnit astTu){
		MessageConsole console = new MessageConsole("My Console", null);
		console.activate();
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
		MessageConsoleStream stream = console.newMessageStream();
		
		IASTDeclaration[] decls = astTu.getDeclarations();
		stream.println("{");
		for (IASTDeclaration declaration : decls) {
			IASTDeclaration tempdeclaration=declaration;
			if(tempdeclaration instanceof ICPPASTTemplateDeclaration){
				ICPPASTTemplateDeclaration ee=(ICPPASTTemplateDeclaration)tempdeclaration;
				tempdeclaration=ee.getDeclaration();
			}
						
			if(tempdeclaration instanceof IASTSimpleDeclaration){
				IASTSimpleDeclaration o=(IASTSimpleDeclaration)tempdeclaration;
				IASTCompositeTypeSpecifier dd=(IASTCompositeTypeSpecifier)o.getDeclSpecifier();
				IASTDeclaration[] ee=dd.getMembers(); 
				for(IASTDeclaration ff:ee){
					if(ff instanceof IASTSimpleDeclaration){
						IASTSimpleDeclaration gg=(IASTSimpleDeclaration)ff;
						for(IASTDeclarator hh:gg.getDeclarators()){
							if(hh instanceof ICPPASTFunctionDeclarator){
								ICPPASTFunctionDeclarator ii=(ICPPASTFunctionDeclarator)hh;
								stream.println(ii.getName().toString());
							}
						}
					}
					
					/*for(IASTDeclarator gg:ff)
					if(gg instanceof ICPPASTFunctionDeclarator){
						//ICPPASTFunctionDeclarator gg=(ICPPASTFunctionDeclarator)ff;
						//ff.getName()
						stream.println(ff.getName().toString());
					}*/
				}
				
				//for (IASTDeclSpecifier bb : dd) {
				//	stream.println(dd.getName().toString());
				
				//IASTFunctionDeclarator p=o.getDeclarator();
				
				//if("operator "+o.OP_PAREN==o.toString())
				//stream.println(o.getName().toString());
			}
				
		}stream.println("}");
		
		
	}
}