package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;

abstract public class AddTestFunct_ION_OR extends AbstractFunctionAction {

	@SuppressWarnings("restriction")
	protected boolean checkPushback(IASTTranslationUnit astTu,String fname,SuitePushBackFinder suitPushBackFinder){
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			for (IASTName name1 : refs) {
				try{
					IASTFieldReference fRef = (ICPPASTFieldReference) name1.getParent().getParent();
					if(fRef.getFieldName().toString().equals("push_back")) {
						IASTFunctionCallExpression callex=(IASTFunctionCallExpression)name1.getParent().getParent().getParent();
						IASTFunctionCallExpression innercallex=(IASTFunctionCallExpression)callex.getParameterExpression();
						IASTExpression thelist=innercallex.getParameterExpression();
						String theName="";
						if(thelist!=null){
							theName=functionAST(thelist);
						}else{
							theName=functorAST(innercallex);
						}
						if(theName.equals(fname)){
							problemMarkerErrorLineNumber=name1.getFileLocation().getStartingLineNumber();
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
	
	protected String functionAST(IASTExpression thelist){
		String theName="";
		if(thelist instanceof IASTExpressionList){//normal run only
			IASTExpression innerlist[]=((IASTExpressionList)thelist).getExpressions();
			IASTUnaryExpression unaryex=(IASTUnaryExpression)innerlist[1];
			IASTLiteralExpression literalex=(IASTLiteralExpression)unaryex.getOperand();
			theName=literalex.toString();
		}else{//both normal run and unit test
			theName=((CPPASTIdExpression)thelist).getName().toString();
		}
		return theName;
	}

	protected String functorAST(IASTFunctionCallExpression innercallex){
		String theName="";
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
		return theName;
	}
}
