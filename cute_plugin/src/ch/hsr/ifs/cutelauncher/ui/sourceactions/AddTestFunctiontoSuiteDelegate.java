package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.InsertEdit;
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
	/*
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {
		for (TextEdit textEdit : edits) {
			String insert = ((InsertEdit)textEdit).getText();
			if(insert.contains(NewTestFunctionAction.TEST_STMT.trim())) {
				return (textEdit.getOffset() + insert.indexOf(NewTestFunctionAction.TEST_STMT.trim()));
			}
		}
		return edits[0].getOffset() + edits[0].getLength();
	}
	@Override
	int getExitPositionLength(){
		return NewTestFunctionAction.TEST_STMT.trim().length();
	}*/
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
				//int insertFileOffset = getInsertOffset(astTu, selection);
				SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
				astTu.accept(suitPushBackFinder);

				//mEdit.addChild(createdEdit(insertFileOffset, doc, funcName));
				String fname=getFunctionNameAtCursor(astTu, selection);
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
	public String getFunctionNameAtCursor(IASTTranslationUnit astTu,TextSelection selection){
		IASTDeclaration selectedNode=getDeclarationAtCursor(astTu,selection);
		IASTNode node = selectedNode;
		while(node != null) {
			if (node instanceof IASTFunctionDefinition) {
				/*IASTName name=((ICPPASTFunctionDeclarator)node).getName();
				return name.toString();*/
				CPPASTFunctionDefinition a1=(CPPASTFunctionDefinition)node;
				IASTFunctionDeclarator b1=a1.getDeclarator();
				IASTName c1=b1.getName();
				return c1.toString();
			}
			node = node.getParent();
		}
		dontAddFlag=true;
		return "";
	}
	//checking existing suite for the name already
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
						IASTFunctionCallExpression ex=(IASTFunctionCallExpression)callex.getParameterExpression();
						CPPASTExpressionList thelist=(CPPASTExpressionList)ex.getParameterExpression();
						IASTExpression unlist[]=thelist.getExpressions();
						CPPASTUnaryExpression aa=(CPPASTUnaryExpression)unlist[1];
						CPPASTLiteralExpression bb=(CPPASTLiteralExpression)aa.getOperand();
						String theName=bb.toString();
						if(theName.equals(fname))return true;
					}
				}
			}
			
		}else ;//TODO need to create suite
		
		return false;
	}
	private TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, String funcName, SuitePushBackFinder suitPushBackFinder) {
		String newLine = TextUtilities.getDefaultLineDelimiter(doc);
		
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			IASTStatement lastPushBack = getLastPushBack(refs);
			if(lastPushBack != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(newLine);
				builder.append("\t");
				builder.append(name.toString());
				builder.append(".push_back(CUTE(");
				builder.append(funcName);
				builder.append("));");
				IASTFileLocation fileLocation = lastPushBack.getFileLocation();
				InsertEdit edit = new InsertEdit(fileLocation.getNodeOffset() + fileLocation.getNodeLength(), builder.toString());
				return edit;
			}else {
				//TODO Errorhandling
				return null;
			}
		}else {
			//TODO Errorhandling
			return null;
		}
		
	}

	/*find the point of last "push_back" */
	private IASTStatement getLastPushBack(IASTName[] refs) {
		IASTName lastPushBack = null;
		for (IASTName name : refs) {
			if(name.getParent().getParent() instanceof ICPPASTFieldReference) {
				IASTFieldReference fRef = (ICPPASTFieldReference) name.getParent().getParent();
				if(fRef.getFieldName().toString().equals("push_back")) {
					lastPushBack = name;
				}
			}
		}
		if(lastPushBack == null);//TODO no pushback found do something abt it
		return getParentStatement(lastPushBack);
	}

	private IASTStatement getParentStatement(IASTName lastPushBack) {
		IASTNode node = lastPushBack;
		while(node != null) {
			if (node instanceof IASTStatement) {
				return (IASTStatement) node;
			}
			node = node.getParent();
		}
		return null;
	}
	
}