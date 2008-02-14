package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
public class AddTestFunctiontoSuiteDelegate extends AbstractFunctionActionDelegate {
	
	public AddTestFunctiontoSuiteDelegate(){
		super("AddTestFunctiontoSuite",new AddTestFunctiontoSuiteAction());
	}
/*
	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		System.out.println("run");
		/*MessageConsole console = new MessageConsole("My Console", null);
		console.activate();
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
		MessageConsoleStream stream = console.newMessageStream();
		stream.println("Hello, world!");*/
	//}*/
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {return 0;}
	@Override
	int getExitPositionLength(){return 0;}
}
class AddTestFunctiontoSuiteAction extends AbstractFunctionAction{
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
				SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
				astTu.accept(suitPushBackFinder);

				String fname=getFunctionNameAtCursor(astTu, selection,suitPushBackFinder);
				if(!dontAddFlag && !checkNameExist(astTu,fname,suitPushBackFinder))
				{
					mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
						fname, suitPushBackFinder));
				}
			}
		}
		return mEdit;
	}
	boolean dontAddFlag=false;
	//find function name within selected cursor location
	public String getFunctionNameAtCursor(IASTTranslationUnit astTu,TextSelection selection,
			SuitePushBackFinder suitPushBackFinder){
		IASTDeclaration selectedNode=getDeclarationAtCursor(astTu,selection);
		IASTNode node = selectedNode;
		dontAddFlag=false;
		while(node != null) {
			if (node instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition functionDefinition=(IASTFunctionDefinition)node;

				IASTSimpleDeclSpecifier specifier=(IASTSimpleDeclSpecifier)functionDefinition.getDeclSpecifier();
				//check for 'void'
				if(specifier.getType()!=IASTSimpleDeclSpecifier.t_void ||
						//don't add the function that cute::suite was declared in, else recursive loop 
						node.contains(suitPushBackFinder.getSuiteNode())){
					dontAddFlag=true;
					return "";
				}
				IASTName name=functionDefinition.getDeclarator().getName();
				return name.toString();
			}
			node = node.getParent();
		}
		dontAddFlag=true;
		return "";
	}

	protected IASTDeclaration getDeclarationAtCursor(IASTTranslationUnit astTu, TextSelection selection) {
		int selOffset = selection.getOffset();
		IASTDeclaration[] decls = astTu.getDeclarations();
		for (IASTDeclaration declaration : decls) {
			int nodeOffset = declaration.getFileLocation().getNodeOffset();
			int nodeLength = declaration.getFileLocation().asFileLocation().getNodeLength();
			if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength)) {
				return declaration;
			}
		}
		return null;
	}
	
	//checking existing suite for the name of the function
	//ensure it is not already added into suite
	public boolean checkNameExist(IASTTranslationUnit astTu,String fname,SuitePushBackFinder suitPushBackFinder){
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			for (IASTName name1 : refs) {
				if(name1.getParent().getParent() instanceof ICPPASTFieldReference) {
					IASTFieldReference fRef = (ICPPASTFieldReference) name1.getParent().getParent();
					if(fRef.getFieldName().toString().equals("push_back")) {
						IASTFunctionCallExpression callex=(IASTFunctionCallExpression)name1.getParent().getParent().getParent();
						IASTFunctionCallExpression innercallex=(IASTFunctionCallExpression)callex.getParameterExpression();
						IASTExpressionList thelist=(IASTExpressionList)innercallex.getParameterExpression();
						IASTExpression innerlist[]=thelist.getExpressions();
						IASTUnaryExpression unaryex=(IASTUnaryExpression)innerlist[1];
						IASTLiteralExpression literalex=(IASTLiteralExpression)unaryex.getOperand();
						String theName=literalex.toString();
						if(theName.equals(fname))return true;
					}
				}
			}
		}else{//TODO need to create suite
			//@see getLastPushBack() for adding the very 1st push back
		}
		
		return false;
	}

}