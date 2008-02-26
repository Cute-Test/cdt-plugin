package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ch.hsr.ifs.cutelauncher.EclipseConsole;
public class AddTestFunctortoSuiteDelegate extends
		AbstractFunctionActionDelegate {
	public AddTestFunctortoSuiteDelegate(){
		super("AddTestFunctortoSuite",new AddTestFunctortoSuiteAction());
	}
	
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {
		return 0;
	}

	@Override
	int getExitPositionLength() {
		return 0;
	}

}
class AddTestFunctortoSuiteAction extends AbstractFunctionAction{
	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException{
		
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;
			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				
				MessageConsoleStream stream = EclipseConsole.getConsole();
				
				//scanforParenthesesOperator1(astTu);
				NodeAtCursorFinder n= new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
				
				OperatorParenthesesFinder o=new OperatorParenthesesFinder();
				astTu.accept(o);
				
				String fname=nameAtCursor(o.getAL(),n.getNode(),stream);
				if(fname.equals(""))return null;
				
				SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
				astTu.accept(suitPushBackFinder);
				
				if(!checkNameExist(astTu,fname,suitPushBackFinder)){//??? +()
					MultiTextEdit mEdit = new MultiTextEdit();
					
					String newLine = TextUtilities.getDefaultLineDelimiter(doc);
					StringBuilder builder = new StringBuilder();
					builder.append(newLine);
					builder.append("\t");
					IASTName name = suitPushBackFinder.getSuiteDeclName();//XXX
					builder.append(name.toString());
					builder.append(".push_back(");
					builder.append(fname+"()");
					builder.append(");");
										
					mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
							suitPushBackFinder,builder));
					return mEdit;
				}
								
				stream.println(n.getBounded()+"wa");
				stream.println(n.get()+"wa");
				stream.println(n.getNode()+"w");
			}
		}
		return null;
	}
	protected String nameAtCursor(ArrayList<IASTName> operatorParenthesesNode,IASTNode node,MessageConsoleStream stream ){
		if(node instanceof IASTStatement){
			//hunt for function call, either within normal function or within a class
			if(node instanceof IASTCompoundStatement){
				IASTStatement a[]=((IASTCompoundStatement)node).getStatements();
				//search for function call close to cursor
				//if non is found, go up and use the function 
				EclipseConsole.println(a[0].toString());
				for(IASTStatement b:a){
					if(b instanceof IASTExpressionStatement){
						//System.out.println(b);
						IASTIdExpression e=(IASTIdExpression)((IASTFunctionCallExpression)(((IASTExpressionStatement)b).getExpression())).getFunctionNameExpression();
						//when user select a method in the normal class
						stream.println(e.getName().toString());
						return e.getName().toString();
					}
				}
			}
		}else if(node instanceof IASTDeclaration){
			if(node instanceof ICPPASTVisiblityLabel){
				//public: private: protected: for class
				node=node.getParent().getParent();
				//FIXME operator() is private,protected in a class/struct??
			}
			
			//check class, struct at cursor for operator()
			boolean operatorMatchFlag=false;
			for(IASTName i:operatorParenthesesNode){
				if(node.contains(i)){
					operatorMatchFlag=true;
					break;
				}
			}if(!operatorMatchFlag)return "";

			//check also operator() doesnt have parameters, or at least default binded
			//check for function not virtual and has a method body
			//visibilitylabel: private cannot
			if(node instanceof IASTSimpleDeclaration){//simple class case
				/*class TFunctor{
					private:
					public:  
				 ***but cannot handle the function within
				}*/
				IASTName i=((IASTCompositeTypeSpecifier)(((IASTSimpleDeclaration)node).getDeclSpecifier())).getName();
				return i.toString();
			}else 
				if(node instanceof ICPPASTTemplateDeclaration){//template class case
					//template <class TClass> 
					IASTName i=((IASTCompositeTypeSpecifier)(((IASTSimpleDeclaration)((ICPPASTTemplateDeclaration)node).getDeclaration()).getDeclSpecifier())).getName();
					return i.toString();
				}
		}
		return ""; 
	}
}