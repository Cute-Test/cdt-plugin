package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import ch.hsr.ifs.cutelauncher.EclipseConsole;

public class AddTestFunctortoSuiteAction extends AbstractFunctionAction{
	private boolean constructorNeedParameterFlag=false;
	private final MessageConsoleStream stream = EclipseConsole.getConsole();
	
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
				
				//FIXME merge the visitors
				NodeAtCursorFinder n= new NodeAtCursorFinder(selection.getOffset());
				astTu.accept(n);
				
				OperatorParenthesesFinder o=new OperatorParenthesesFinder();
				astTu.accept(o);
				
				ArrayList al=o.getAL();
								
				String fname=nameAtCursor(o.getAL(),n.getNode(),stream);
				if(fname.equals(""))return new MultiTextEdit();//FIXME potential bug point

				constructorNeedParameterFlag=checkForConstructorWithParameters(astTu,n.getNode());
				
				SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
				astTu.accept(suitPushBackFinder);
				
				//if(!checkNameExist(astTu,fname,suitPushBackFinder)){
				if(!checkPushback(astTu,fname,suitPushBackFinder)){
					MultiTextEdit mEdit = new MultiTextEdit();
					
					StringBuilder builder = new StringBuilder();
					
					String insidePushback;
					if(constructorNeedParameterFlag)insidePushback=(fname+"(pArAmEtRs_ReQuIrEd)");
					else insidePushback=(fname+"()");
					builder.append(PushBackString(suitPushBackFinder.getSuiteDeclName().toString(),insidePushback));
					
					mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
							suitPushBackFinder,builder));
					return mEdit;
				}
			}
		}
		return new MultiTextEdit();
	}
	
	protected String nameAtCursor(ArrayList<IASTName> operatorParenthesesNode,IASTNode node,MessageConsoleStream stream ){
		if(node instanceof IASTDeclaration){
			if(node instanceof ICPPASTVisiblityLabel){
				//public: private: protected: for class
				node=node.getParent().getParent();
				//FIXME operator() is private,protected in a class/struct??
			}
			
			try{
				//check class for public operator() 
				IASTNode tmp1=node;
				while(!(tmp1.getParent() instanceof ICPPASTCompositeTypeSpecifier) && !(tmp1.getParent() instanceof ICPPASTTranslationUnit)){
					tmp1=tmp1.getParent();
				}
				if(tmp1.getParent() instanceof ICPPASTCompositeTypeSpecifier)tmp1=tmp1.getParent().getParent();
				if(tmp1 instanceof IASTSimpleDeclaration){
					ArrayList<IASTDeclaration> al=ASTHelper.getPublicMethods((IASTSimpleDeclaration)tmp1);
					boolean publicOperatorExist=false;
					for(IASTDeclaration i:al){
						if(ASTHelper.getMethodName(i).equals("operator ()")){
							publicOperatorExist=true;break;
						}
					}
					if(!publicOperatorExist){
						stream.println("no public operator ()");
						return "";
					}
				}
				System.out.println("");
			}catch(NullPointerException npe){npe.printStackTrace();}
			catch(ClassCastException cce){cce.printStackTrace();}
			
			
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
			/*if(node instanceof ICPPASTTemplateDeclaration){
				//template <class TClass> 
				//shouldnt happen as requires the template to be initialised
				stream.println("template class declarations selected, unable to add as functor.");
				//IASTName i=((IASTCompositeTypeSpecifier)(((IASTSimpleDeclaration)((ICPPASTTemplateDeclaration)node).getDeclaration()).getDeclSpecifier())).getName();
				//return i.toString();
				return "";
			}*/
			IASTNode checkforTemplate=node;
			//template class case
			while(!(checkforTemplate instanceof ICPPASTTranslationUnit)){
				if(checkforTemplate instanceof ICPPASTTemplateDeclaration){
					stream.println("template class declarations selected, unable to add as functor. (2)");return "";
				}
				checkforTemplate=checkforTemplate.getParent();
			}
			
			if(!operatorMatchFlag){
				stream.println("no matching operator() found at current cursor location.");
				if(getWantedTypeParent(node).getParent() instanceof IASTTranslationUnit){
					stream.println("function selected.");//TODO trigger addfunctiontosuite
				}
				return "";
			}
			
			//TODO check also operator() doesnt have parameters, or at least default binded
			//TODO check for function not virtual and has a method body
			if(node instanceof IASTSimpleDeclaration){//simple class case
				/*class TFunctor{
					private:
					public:  
				 ***but cannot handle the methods 
				}*/
				IASTDeclSpecifier aa=(((IASTSimpleDeclaration)node).getDeclSpecifier());
				if(null!=aa && aa instanceof IASTCompositeTypeSpecifier){
					IASTName i=((IASTCompositeTypeSpecifier)aa).getName();
					return i.toString();
				}
			}	
		}
		
		//FIXME wouldnt be detected as preprocess statement NodeAtCursorFinder returns null
		/*if(node instanceof IASTPreprocessorStatement){
			stream.println("preprocessor statement selected, unable to add as functor.");
			return "";
		}*/

		IASTNode parentNode=getWantedTypeParent(node);
		if(parentNode instanceof IASTFunctionDefinition || 
				parentNode instanceof IASTSimpleDeclaration){
				//handle the simple class case, cursor at methods
			//if(!(parentNode.getParent() instanceof ICPPASTTranslationUnit))	
			return ((CPPASTCompositeTypeSpecifier)(parentNode.getParent())).getName().toString();
		}
		stream.println("Unable to add as functor for cursor position.");
		return ""; 
	}
	
	public IASTNode getWantedTypeParent(IASTNode node){
		IASTNode parentNode=node, prevNode=node;
		while(!(parentNode instanceof IASTFunctionDefinition ||
				parentNode instanceof IASTSimpleDeclaration||
				parentNode instanceof ICPPASTTranslationUnit)){
			try{
			prevNode=parentNode;	
			parentNode=parentNode.getParent();
			}catch(NullPointerException npe){return prevNode;}
		}return parentNode;
	}

	public boolean checkForConstructorWithParameters(IASTTranslationUnit astTu,IASTNode node){
		FunctionFinder ff=new FunctionFinder();
		astTu.accept(ff);
		for(Object i:ff.getClassStruct()){
			//stream.println(ff.getSimpleDeclarationNodeName((IASTSimpleDeclaration)(i))+"");
			if(((IASTNode)i).contains(node)){
				ArrayList<IASTDeclaration> constructors=ASTHelper.getConstructors((IASTSimpleDeclaration)i);				
				return ASTHelper.haveParameters(constructors);
			}
		}
		return false;
	}
	private boolean checkPushback(IASTTranslationUnit astTu,String fname,SuitePushBackFinder suitPushBackFinder){
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			for (IASTName name1 : refs) {
				try{
				if(name1.getParent().getParent() instanceof ICPPASTFieldReference) {
					IASTFieldReference fRef = (ICPPASTFieldReference) name1.getParent().getParent();
					if(fRef.getFieldName().toString().equals("push_back")) {
						IASTFunctionCallExpression callex=(IASTFunctionCallExpression)name1.getParent().getParent().getParent();
						IASTFunctionCallExpression innercallex=(IASTFunctionCallExpression)callex.getParameterExpression();
						IASTExpression thelist=innercallex.getParameterExpression();
						String theName="";
						if(thelist!=null){
						}else{
							if(innercallex instanceof CPPASTIdExpression){
								CPPASTIdExpression a=(CPPASTIdExpression)innercallex.getFunctionNameExpression();
								theName=a.getName().toString();
							}else if(innercallex instanceof CPPASTFunctionCallExpression){
								CPPASTFunctionCallExpression fce=(CPPASTFunctionCallExpression)innercallex;
								IASTExpression expression=fce.getFunctionNameExpression();
								if(expression instanceof CPPASTFieldReference){
									CPPASTFieldReference a=(CPPASTFieldReference)expression;
									theName=a.getFieldName().toString();	
								}
								if(expression instanceof CPPASTIdExpression){
									CPPASTIdExpression a=(CPPASTIdExpression)expression;
									theName=a.getName().toString();
								}
							}
						}
						if(theName.equals(fname))return true;
					}
				}
				}catch(ClassCastException e){}
			}
		}else{//TODO need to create suite
			//@see getLastPushBack() for adding the very 1st push back
		}
		
		return false;
		
	}

}
