package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;

public class OperatorParenthesesFinder extends ASTVisitor {
	ArrayList al=new ArrayList();
	
	{
		shouldVisitNames=true;
		//shouldVisitDeclarators=true;
	}
	
	@Override
	public int leave(IASTName name) {
		if(name.toString().equals("operator ()"))al.add(name);
		return super.leave(name);
	}
	public ArrayList getAL(){return al;}
}
