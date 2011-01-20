package ch.hsr.ifs.cute.ui.test.sourceactions;

import junit.framework.Test;
import junit.framework.TestSuite;
public class SourceActionsTest {

	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.sourceactions"); //$NON-NLS-1$

		ts.addTest(TestBugFixes.suite());
//		ts.addTestSuite(AddTestToSuiteTest.class);
//		ts.addTestSuite(NewTestFunctionTest.class);
		
		return ts;
	}
	
}

