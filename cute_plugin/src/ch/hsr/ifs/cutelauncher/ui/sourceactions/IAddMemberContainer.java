package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

interface IAddMemberContainer {

	public static final boolean InstanceType = true;
	public static final boolean ClassType = false;
	public final boolean isInstance=false;
	public String classTypeName="";
	
	public abstract void add(Object element);

	public abstract String toString();
	public abstract void setSimpleDeclaration(IASTSimpleDeclaration simpleDeclaration);
	public abstract IASTSimpleDeclaration getSimpleDeclaration();
	public abstract void setMethods(ArrayList<Method> methods);
	public abstract ArrayList<Method> getMethods();
}