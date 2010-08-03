/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */
public class AddFunctorToSuiteStrategy extends AddStrategy {
	
	private IASTTranslationUnit astTu;
	private boolean constructorNeedParameterFlag=false;
	private IASTNode node;
	private IFile editorFile;

	public AddFunctorToSuiteStrategy(IDocument doc, IASTTranslationUnit astTu, IASTNode node, IFile editorFile) {
		super(doc);
		this.astTu = astTu;
		this.node = node;
		this.editorFile = editorFile;
	}

	@Override
	public MultiTextEdit getEdit() {
		OperatorParenthesesFinder o=new OperatorParenthesesFinder();
		astTu.accept(o);

		String fname=nameAtCursor(o.getAL(),node);
		if(fname.equals(EMPTY_STRING))return new MultiTextEdit();//FIXME potential bug point

		constructorNeedParameterFlag=checkForConstructorWithParameters(astTu,node);

		SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
		astTu.accept(suitPushBackFinder);

		if(!checkPushback(astTu,fname,suitPushBackFinder)){
			MultiTextEdit mEdit = new MultiTextEdit();

			StringBuilder builder = new StringBuilder();

			String insidePushback;
			if(constructorNeedParameterFlag)insidePushback=(fname+"(pArAmEtRs_ReQuIrEd)"); //$NON-NLS-1$
			else insidePushback=(fname+"()"); //$NON-NLS-1$
			builder.append(pushBackString(suitPushBackFinder.getSuiteDeclName().toString(),insidePushback));

			mEdit.addChild(createPushBackEdit(editorFile, astTu,
					suitPushBackFinder,builder));
			return mEdit;
		}
		return new MultiTextEdit();
	}
	
	private boolean checkForConstructorWithParameters(IASTTranslationUnit astTu,IASTNode node){
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
	
	private boolean checkClassForPublicOperatorParentesis(IASTNode node) {
		IASTNode tmp1=node;
		while(!(tmp1.getParent() instanceof ICPPASTCompositeTypeSpecifier) && !(tmp1.getParent() instanceof ICPPASTTranslationUnit)){
			tmp1=tmp1.getParent();
		}
		if(tmp1.getParent() instanceof ICPPASTCompositeTypeSpecifier)tmp1=tmp1.getParent().getParent();
		
		boolean publicOperatorExist=false;
		if(tmp1 instanceof IASTSimpleDeclaration){
			ArrayList<IASTDeclaration> al=ASTHelper.getPublicMethods((IASTSimpleDeclaration)tmp1);
			for(IASTDeclaration i:al){
				if(ASTHelper.getMethodName(i).equals(Messages.getString("AddTestFunctortoSuiteAction.Operator"))){ //$NON-NLS-1$
					publicOperatorExist=true;break;
				}
			}
		}
		return publicOperatorExist;
	}
	
	private boolean isTemplateClass(IASTNode checkforTemplate) {
		while(!(checkforTemplate instanceof ICPPASTTranslationUnit)){
			if(checkforTemplate instanceof ICPPASTTemplateDeclaration){
				return true;
			}
			checkforTemplate=checkforTemplate.getParent();
		}
		return false;
	}
	
	//handle case of virtual operator not declared
	private boolean isVirtualOperatorDeclared(
			ArrayList<IASTName> operatorParenthesesNode, IASTNode node,
			boolean operatorMatchFlag) {
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
		return operatorMatchFlag;
	}
	
	private IASTNode getWantedTypeParent(IASTNode node){
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
	
	protected String nameAtCursor(ArrayList<IASTName> operatorParenthesesNode,IASTNode node){
		if(node instanceof IASTDeclaration){
			if(node instanceof ICPPASTVisibilityLabel){
				//public: private: protected: for class
				node=node.getParent().getParent();
				//FIXME operator() is private,protected in a class/struct??
			}
			
			try{
			
				boolean flag=checkClassForPublicOperatorParentesis(node);
				if(!flag){return EMPTY_STRING;}
			
			}catch(NullPointerException npe){npe.printStackTrace();}
			catch(ClassCastException cce){cce.printStackTrace();}
			
			boolean flag=isTemplateClass(node);
			if(flag){return EMPTY_STRING;}
	
			
			
			//check class, struct at cursor for operator()
			boolean operatorMatchFlag=false;
			for(IASTName i:operatorParenthesesNode){
				if(node.contains(i)){
					operatorMatchFlag=true;
					break;
				}
			}
						
			operatorMatchFlag = isVirtualOperatorDeclared(
					operatorParenthesesNode, node, operatorMatchFlag);
						
			if(!operatorMatchFlag){
				return EMPTY_STRING;
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
			return ((ICPPASTCompositeTypeSpecifier)(parentNode.getParent())).getName().toString();
		}
		return EMPTY_STRING; 
	}


}
