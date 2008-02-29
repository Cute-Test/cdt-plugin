package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ch.hsr.ifs.cutelauncher.EclipseConsole;

public class AddTestFunctortoSuiteAction extends AbstractFunctionAction{
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
				//FIXME merge the vistors
				NodeAtCursorFinder n= new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
				
				OperatorParenthesesFinder o=new OperatorParenthesesFinder();
				astTu.accept(o);
				
				String fname=nameAtCursor(o.getAL(),n.getNode(),stream);
				if(fname.equals(""))return new MultiTextEdit();//FIXME potential bug point
				
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
		return new MultiTextEdit();
	}
	protected String nameAtCursor(ArrayList<IASTName> operatorParenthesesNode,IASTNode node,MessageConsoleStream stream ){
		/*if(node instanceof IASTStatement){
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
		}*/
		if(node instanceof IASTDeclaration){
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
			}
			if(node instanceof IASTSimpleDeclaration || node instanceof IASTFunctionDefinition){
				if(node.getParent() instanceof ICPPASTCompositeTypeSpecifier){
					IASTNode tmp=node.getParent();
					for(IASTName i:operatorParenthesesNode){
						if(tmp.contains(i)){
							operatorMatchFlag=true;
							break;
						}
					}
				}
			}
			if(!operatorMatchFlag){stream.println("no matching operator() found at current cursor location.");return "";}

			//check also operator() doesnt have parameters, or at least default binded
			//check for function not virtual and has a method body
			if(node instanceof IASTSimpleDeclaration){//simple class case
				/*class TFunctor{
					private:
					public:  
				 ***but cannot handle the function within
				}*/
				IASTDeclSpecifier aa=(((IASTSimpleDeclaration)node).getDeclSpecifier());
				if(null!=aa && aa instanceof IASTCompositeTypeSpecifier){
					IASTName i=((IASTCompositeTypeSpecifier)aa).getName();
					return i.toString();
				}
			}else 
				if(node instanceof ICPPASTTemplateDeclaration){//template class case
					//template <class TClass> 
					//shouldnt happen as requires the template to be initialised
					stream.println("template class declarations selected, unable to add as functor.");
					//IASTName i=((IASTCompositeTypeSpecifier)(((IASTSimpleDeclaration)((ICPPASTTemplateDeclaration)node).getDeclaration()).getDeclSpecifier())).getName();
					//return i.toString();
					return "";
				}
		}
		
		IASTNode parentNode=node;
		while(!(parentNode instanceof IASTFunctionDefinition ||
				parentNode instanceof IASTSimpleDeclaration||
				parentNode instanceof ICPPASTTranslationUnit)){
			parentNode=parentNode.getParent();
		}
		if(parentNode instanceof IASTFunctionDefinition){
			stream.println("IASTFunctionDefinition");
			if(parentNode.getParent() instanceof CPPASTCompositeTypeSpecifier)
				//handle the simple class case, cursor at methods
				return ((CPPASTCompositeTypeSpecifier)(parentNode.getParent())).getName().toString();
			if(parentNode.getParent() instanceof IASTTranslationUnit){
				stream.println("function selected. TODO trigger addfunctiontosuite");
			}
		}else if(parentNode instanceof IASTSimpleDeclaration){
			if(parentNode.getParent() instanceof CPPASTCompositeTypeSpecifier)
				//handle the simple class case, cursor at methods
				return ((CPPASTCompositeTypeSpecifier)(parentNode.getParent())).getName().toString();
		}else if(parentNode instanceof ICPPASTTranslationUnit){
			stream.println("ICPPASTTranslationUnit");
		}
		return ""; 
	}
}