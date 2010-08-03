/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Emanuel Graf
 *
 */
@SuppressWarnings("deprecation")
public class NewTestFunctionAction extends AbstractFunctionAction{
	//TODO create Strategy or new Superclass
	
	protected static final String TEST_STMT = "\tASSERTM(\"start writing tests\", false);"; //$NON-NLS-1$
	int problemMarkerErrorLineNumber=0;
	/**
	 * @since 4.0
	 */
	protected int insertFileOffset;
	/**
	 * @since 4.0
	 */
	protected int pushbackOffset;
	/**
	 * @since 4.0
	 */
	protected int pushbackLength;
	private String newLine;
	private String funcName;
	
	
	/**
	 * @since 4.0
	 */
	public NewTestFunctionAction(String funcName) {
		super();
		this.funcName = funcName;
	}

	@Override
	public MultiTextEdit createEdit(ITextEditor ceditor,
			IEditorInput editorInput, IDocument doc, ISelection sel)
			throws CoreException {
		
		insertFileOffset=-1;
		pushbackOffset=-1;
		pushbackLength=-1;
		problemMarkerErrorLineNumber=0;
		newLine = TextUtilities.getDefaultLineDelimiter(doc);
		
		MultiTextEdit mEdit = new MultiTextEdit();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;

			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				insertFileOffset = getInsertOffset(astTu, selection, doc);

				SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
				astTu.accept(suitPushBackFinder);
				
				mEdit.addChild(createdEdit(insertFileOffset, doc, funcName));

				if(!checkPushback(astTu,funcName,suitPushBackFinder))
				mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
						funcName, suitPushBackFinder));
				else{
					createProblemMarker((FileEditorInput) editorInput, Messages.getString("NewTestFunctionAction.DuplicatedPushback"), problemMarkerErrorLineNumber); //$NON-NLS-1$
				}
			}
		}
		return mEdit;
	}
	
	protected TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, String funcName, SuitePushBackFinder suitPushBackFinder) {
		StringBuilder builder = new StringBuilder();
		builder.append(pushBackString(suitPushBackFinder.getSuiteDeclName().toString(),"CUTE("+funcName+")")); //$NON-NLS-1$ //$NON-NLS-2$
		return createPushBackEdit(editorFile,astTu,suitPushBackFinder,builder);
	}
	
	/**
	 * @since 4.0
	 */
	protected String pushBackString(String suite, String insidePushback){
		StringBuilder builder = new StringBuilder();
		builder.append(newLine+"\t"); //$NON-NLS-1$
		builder.append(suite.toString());
		builder.append(".push_back("); //$NON-NLS-1$
		builder.append(insidePushback);
		builder.append(");"); //$NON-NLS-1$
		return builder.toString();
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
	
	/**
	 * @since 4.0
	 */
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

	//adding the new test function
	private TextEdit createdEdit(int insertTestFuncFileOffset, IDocument doc, String funcName) {
		StringBuilder builder = new StringBuilder();
		builder.append("void "); //$NON-NLS-1$
		builder.append(funcName);
		builder.append("(){"); //$NON-NLS-1$
		builder.append(newLine);
		builder.append(TEST_STMT);
		builder.append(newLine);
		builder.append("}"); //$NON-NLS-1$
		builder.append(newLine);
		builder.append(newLine);
		TextEdit iedit = new InsertEdit(insertTestFuncFileOffset, builder.toString());
		return iedit;
	}

	//checking existing suite for the name of the function
	//ensure it is not already added into suite
	private boolean checkPushback(IASTTranslationUnit astTu,String fname,SuitePushBackFinder suitPushBackFinder){
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			for (IASTName name1 : refs) {
				try{
					IASTFieldReference fRef = (ICPPASTFieldReference) name1.getParent().getParent();
					if(fRef.getFieldName().toString().equals("push_back")) { //$NON-NLS-1$
						IASTFunctionCallExpression callex=(IASTFunctionCallExpression)name1.getParent().getParent().getParent();
						IASTExpression innercallex=callex.getParameterExpression();
						IASTFunctionCallExpression innercallex1=(IASTFunctionCallExpression)innercallex;
						IASTExpression thelist=innercallex1.getParameterExpression();
						String theName=""; //$NON-NLS-1$
						if(thelist!=null){
							if(thelist instanceof IASTExpressionList){//known issue:path executed during normal program run
								//**** block not executed in UNIT Test
								IASTExpression innerlist[]=((IASTExpressionList)thelist).getExpressions();
								IASTUnaryExpression unaryex=(IASTUnaryExpression)innerlist[1];
								IASTLiteralExpression literalex=(IASTLiteralExpression)unaryex.getOperand();
								theName=literalex.toString();
							}else{//path executed during unit testing
								theName=((IASTIdExpression)thelist).getName().toString();
							}
						}
						if(theName.equals(fname)){
							problemMarkerErrorLineNumber=name1.getFileLocation().getStartingLineNumber();
							return true;
						}
					}
					
				}catch(ClassCastException e){}
			}	
		}else{//TODO need to create suite
			
			//@see AbstractFunctionAction.getLastPushBack() for adding the very 1st push back
		}
		
		return false;
	}
	
	//shift the insertion point out syntactical block, relative to user(selection point/current cursor)location
	protected int getInsertOffset(IASTTranslationUnit astTu, TextSelection selection, IDocument doc) {
		int selOffset = selection.getOffset();
		IASTDeclaration[] decls = astTu.getDeclarations();
		for (IASTDeclaration declaration : decls) {
			int nodeOffset = declaration.getFileLocation().getNodeOffset();
			int nodeLength = declaration.getFileLocation().asFileLocation().getNodeLength();
			if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength)) {
				return (nodeOffset);
			}
		}

		//Shift out of preprocessor statements
		// >#include "cute.h<"
		IASTPreprocessorStatement[] listPreprocessor=astTu.getAllPreprocessorStatements();
		for(int x=0;x<listPreprocessor.length;x++){
			int nodeOffset = listPreprocessor[x].getFileLocation().getNodeOffset();
			int nodeLength = listPreprocessor[x].getFileLocation().asFileLocation().getNodeLength();
			if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength)) {
				return nodeOffset;
			}
		}

		try{
		int selectedLineNo=selection.getStartLine();
		IRegion iregion= doc.getLineInformation(selectedLineNo);
		String text=doc.get(iregion.getOffset(), iregion.getLength());
		if(text.startsWith("#include")){ //$NON-NLS-1$
			return iregion.getOffset();
		}
		
		}catch(org.eclipse.jface.text.BadLocationException be){}
		
		//just use the user selection if no match, it could possibly mean that the cursor at the 
		//very end of the source file
		return selOffset;
	}
	
	
	
	
	/**
	 * @since 4.0
	 */
	public static TextEdit testOnlyCreatedEdit(int insertTestFuncFileOffset){
		NewTestFunctionAction ntfa=new NewTestFunctionAction("newTestFunction"); //$NON-NLS-1$
		return ntfa.createdEdit(insertTestFuncFileOffset, null, "newTestFunction"); //$NON-NLS-1$
	}
	
	/**
	 * @since 4.0
	 */
	public static TextEdit testOnlyPushBackString(int insertloc){
		
		NewTestFunctionAction ntfa=new NewTestFunctionAction(null);
		String s=ntfa.pushBackString("s","CUTE(newTestFunction)"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuilder builder = new StringBuilder();
		builder.append(s);
		
		InsertEdit edit = new InsertEdit(insertloc, builder.toString());
		return edit;
	}
}