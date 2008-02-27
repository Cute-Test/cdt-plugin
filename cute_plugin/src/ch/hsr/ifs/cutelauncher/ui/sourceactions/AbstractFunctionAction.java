package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

public abstract class AbstractFunctionAction {
	public abstract MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException;
	
	//return the CDT representation of the file under modification 
	protected IASTTranslationUnit getASTTranslationUnit(IFile editorFile)
			throws CoreException {
		ITranslationUnit tu = CoreModelUtil.findTranslationUnit(editorFile);
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());	
		IASTTranslationUnit astTu = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		return astTu;
	}

	//shift the insertion point out syntactical block, relative to user(selection point/current cursor)location
	protected int getInsertOffset(IASTTranslationUnit astTu, TextSelection selection) {
		int selOffset = selection.getOffset();
		IASTDeclaration[] decls = astTu.getDeclarations();
		for (IASTDeclaration declaration : decls) {
			int nodeOffset = declaration.getFileLocation().getNodeOffset();
			int nodeLength = declaration.getFileLocation().asFileLocation().getNodeLength();
			if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength)) {
				return (nodeOffset);
			}else if(selOffset <= nodeOffset) {
				return selOffset;
			}
		}
		return selOffset;
	}
	
	protected TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, String funcName, SuitePushBackFinder suitPushBackFinder) {
		String newLine = TextUtilities.getDefaultLineDelimiter(doc);
		StringBuilder builder = new StringBuilder();
		builder.append(newLine);
		builder.append("\t");
		IASTName name = suitPushBackFinder.getSuiteDeclName();//XXX
		builder.append(name.toString());
		builder.append(".push_back(CUTE(");
		builder.append(funcName);
		builder.append("));");
		return createPushBackEdit(editorFile,doc,astTu,suitPushBackFinder,builder);
	}
	protected TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, SuitePushBackFinder suitPushBackFinder, StringBuilder builder) {
				
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			IASTStatement lastPushBack = getLastPushBack(refs);

			if(lastPushBack != null) {
				IASTFileLocation fileLocation = lastPushBack.getFileLocation();
				InsertEdit edit = new InsertEdit(fileLocation.getNodeOffset() + fileLocation.getNodeLength(), builder.toString());
				return edit;
			}else {//case where no push_back was found, use cute::suite location 
				IASTFileLocation fileLocation = suitPushBackFinder.getSuiteNode().getParent().getFileLocation();
				InsertEdit edit = new InsertEdit(fileLocation.getNodeOffset() + fileLocation.getNodeLength(), builder.toString());
				return edit;
			}
		}else {
			//TODO case of no cute::suite found
			
			return null;
		}
	}
	
	/*find the point of last "push_back" */
	protected IASTStatement getLastPushBack(IASTName[] refs) {
		IASTName lastPushBack = null;
		for (IASTName name : refs) {
			if(name.getParent().getParent() instanceof ICPPASTFieldReference) {
				IASTFieldReference fRef = (ICPPASTFieldReference) name.getParent().getParent();
				if(fRef.getFieldName().toString().equals("push_back")) {
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
						IASTExpression thelist=innercallex.getParameterExpression();
						String theName;
						if(thelist!=null){
							if(thelist instanceof IASTExpressionList){//????
								IASTExpression innerlist[]=((IASTExpressionList)thelist).getExpressions();
								IASTUnaryExpression unaryex=(IASTUnaryExpression)innerlist[1];
								IASTLiteralExpression literalex=(IASTLiteralExpression)unaryex.getOperand();
								theName=literalex.toString();
							}else{//for newtestfunction , addfunction
								theName=((CPPASTIdExpression)thelist).getName().toString();
							}
						}else{//handle functor nodes
							CPPASTIdExpression a=(CPPASTIdExpression)innercallex.getFunctionNameExpression();
							theName=a.getName().toString();
						}
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
//http://www.ibm.com/developerworks/library/os-ecl-cdt3/index.html?S_TACT=105AGX44&S_CMP=EDU
//Building a CDT-based editor