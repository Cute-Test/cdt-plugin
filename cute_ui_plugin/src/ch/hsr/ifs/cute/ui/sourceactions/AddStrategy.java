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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Emanuel Graf IFS
 *
 */
public abstract class AddStrategy {
	
	protected static final String EMPTY_STRING = "";
	protected int insertFileOffset=-1; //for NewTestFunctionAction use only, need to reset value in createEdit
	protected int pushbackOffset=-1;   //for NewTestFunctionAction use only, need to reset value in createEdit
	protected int pushbackLength=-1;   //for NewTestFunctionAction use only, need to reset value in createEdit
	protected String newLine;
	
	

	public AddStrategy(IDocument doc) {
		newLine = TextUtilities.getDefaultLineDelimiter(doc);
	}

	public abstract MultiTextEdit getEdit();
	
	protected TextEdit createPushBackEdit(IFile editorFile, IASTTranslationUnit astTu, SuitePushBackFinder suitPushBackFinder, StringBuilder builder) {
		
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			IASTStatement lastPushBack = getLastPushBack(refs);

			IASTFileLocation fileLocation; 
			if(lastPushBack != null) {
				fileLocation = lastPushBack.getFileLocation();
			}else {//case where no push_back was found, use cute::suite location 
				fileLocation = suitPushBackFinder.getSuiteNode().getParent().getFileLocation();
			}
			pushbackOffset=fileLocation.getNodeOffset() + fileLocation.getNodeLength();
			InsertEdit edit = new InsertEdit(pushbackOffset, builder.toString());
			pushbackLength=builder.toString().length();
			
			return edit;
		}else {
			//TODO case of no cute::suite found
			
			return null;
		}
	}
	
	protected IASTStatement getParentStatement(IASTName lastPushBack) {
		IASTNode node = lastPushBack;
		while(node != null) {
			if (node instanceof IASTStatement) {
				return (IASTStatement) node;
			}
			node = node.getParent();
		}
		return null;
	}
	
	/*find the point of last "push_back" */
	protected IASTStatement getLastPushBack(IASTName[] refs) {
		IASTName lastPushBack = null;
		for (IASTName name : refs) {
			if(name.getParent().getParent() instanceof ICPPASTFieldReference) {
				IASTFieldReference fRef = (ICPPASTFieldReference) name.getParent().getParent();
				if(fRef.getFieldName().toString().equals("push_back")) { //$NON-NLS-1$
					lastPushBack = name;
				}
			}
		}
		return getParentStatement(lastPushBack);
	}
	
	protected String pushBackString(String suite, String insidePushback){
		StringBuilder builder = new StringBuilder();
		builder.append(newLine+"\t"); //$NON-NLS-1$
		builder.append(suite.toString());
		builder.append(".push_back("); //$NON-NLS-1$
		builder.append(insidePushback);
		builder.append(");"); //$NON-NLS-1$
		return builder.toString();
	}
	

	protected TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, String funcName, SuitePushBackFinder suitPushBackFinder) {
		StringBuilder builder = new StringBuilder();
		builder.append(pushBackString(suitPushBackFinder.getSuiteDeclName().toString(),"CUTE("+funcName+")")); //$NON-NLS-1$ //$NON-NLS-2$
		return createPushBackEdit(editorFile,astTu,suitPushBackFinder,builder);
	}
	
	protected String functionAST(IASTExpression thelist){
		String theName=EMPTY_STRING;
		if(thelist instanceof IASTExpressionList){//normal run only
			IASTExpression innerlist[]=((IASTExpressionList)thelist).getExpressions();
			IASTUnaryExpression unaryex=(IASTUnaryExpression)innerlist[1];
			IASTLiteralExpression literalex=(IASTLiteralExpression)unaryex.getOperand();
			theName=literalex.toString();
		}else{//both normal run and unit test
			theName=((IASTIdExpression)thelist).getName().toString();
		}
		return theName;
	}

	protected String functorAST(IASTFunctionCallExpression innercallex){
		String theName=EMPTY_STRING;
		if(innercallex instanceof IASTIdExpression){
			IASTIdExpression a=(IASTIdExpression)innercallex.getFunctionNameExpression();
			theName=a.getName().toString();
		}else if(innercallex instanceof IASTFunctionCallExpression){
			IASTFunctionCallExpression fce=innercallex;
			IASTExpression expression=fce.getFunctionNameExpression();
			if(expression instanceof ICPPASTFieldReference){
				ICPPASTFieldReference a=(ICPPASTFieldReference)expression;
				theName=a.getFieldName().toString();	
			}
			if(expression instanceof IASTIdExpression){
				IASTIdExpression a=(IASTIdExpression)expression;
				theName=a.getName().toString();
			}
		}
		return theName;
	}

	protected boolean checkPushback(IASTTranslationUnit astTu,String fname,SuitePushBackFinder suitPushBackFinder){
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			for (IASTName name1 : refs) {
				try{
					IASTFieldReference fRef = (ICPPASTFieldReference) name1.getParent().getParent();
					if(fRef.getFieldName().toString().equals("push_back")) { //$NON-NLS-1$
						IASTFunctionCallExpression callex=(IASTFunctionCallExpression)name1.getParent().getParent().getParent();
						IASTFunctionCallExpression innercallex=(IASTFunctionCallExpression)callex.getParameterExpression();
						IASTExpression thelist=innercallex.getParameterExpression();
						String theName=EMPTY_STRING;
						if(thelist!=null){
							theName=functionAST(thelist);
						}else{
							theName=functorAST(innercallex);
						}
						if(theName.equals(fname)){
							return true;
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
