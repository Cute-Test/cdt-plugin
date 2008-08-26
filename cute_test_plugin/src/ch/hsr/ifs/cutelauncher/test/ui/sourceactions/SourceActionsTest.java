package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.Test;
import junit.framework.TestSuite;
public class SourceActionsTest {

	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.sourceactions");

//		ts.addTest(MemoryEFS.suite());
		
		boolean speedupMode=false;
		ts.addTest(TestNewFunction.suite(speedupMode));
		ts.addTest(TestAddFunction.suite(speedupMode));
		ts.addTest(TestAddFunctor.suite(speedupMode));
				
		TestSuite addMemberTS=TestAddMemberTree.suite();
		TestAddMember.generateMemberTest(addMemberTS);
		ts.addTest(addMemberTS);
		
//		ts.addTest(new TestAddFunction("testDisplayDynamicProxyRecordedResult"));
		
		ts.addTest(NewTestFunctionActionDelegateTest.suite());
		ts.addTest(TestProblemMarkers.suite());
		ts.addTest(TestBugFixes.suite());
		
		return ts;
	}
	
}

