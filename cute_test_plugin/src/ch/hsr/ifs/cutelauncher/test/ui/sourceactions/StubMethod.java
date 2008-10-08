package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import ch.hsr.ifs.cute.ui.sourceactions.IAddMemberContainer;
import ch.hsr.ifs.cute.ui.sourceactions.IAddMemberMethod;

public class StubMethod implements IAddMemberMethod {

	String name;
	IAddMemberContainer parent; 
	
	public StubMethod(String s,IAddMemberContainer parent){
		this.name=s;
		this.parent=parent;
	}
	public IAddMemberContainer getParent() {
		return parent;
	}
	@Override
	public String toString(){
		return name;
	}
}
