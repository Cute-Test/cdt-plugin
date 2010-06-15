package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.Test;
import junit.framework.TestSuite;
public class SourceActionsTest {

	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.sourceactions");

		ts.addTest(TestBugFixes.suite());
		
		return ts;
	}
	
}

