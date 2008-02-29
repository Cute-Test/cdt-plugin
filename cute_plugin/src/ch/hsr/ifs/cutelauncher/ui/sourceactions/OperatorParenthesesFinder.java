package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

public class OperatorParenthesesFinder extends ASTVisitor {
	ArrayList al=new ArrayList();
	
	{
		shouldVisitNames=true;
		//shouldVisitDeclarators=true;
	}
	
	@Override
	public int leave(IASTName name) {
		if(name.toString().equals("operator ()")){
			if(name.getParent() instanceof ICPPASTFunctionDeclarator){
				ICPPASTFunctionDeclarator fdeclarator=(ICPPASTFunctionDeclarator)name.getParent();
				IASTParameterDeclaration fpara[]=fdeclarator.getParameters();
				if(!fdeclarator.takesVarArgs() && fpara.length==0)al.add(name);
			}
					}
		return super.leave(name);
	}
	public ArrayList getAL(){return al;}
}
