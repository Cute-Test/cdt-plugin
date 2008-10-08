package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.TestSuite;
import ch.hsr.ifs.cute.ui.sourceactions.AddTestMembertoSuiteAction;
import ch.hsr.ifs.cute.ui.sourceactions.IAddMemberContainer;
import ch.hsr.ifs.cute.ui.sourceactions.IAddMemberMethod;

public class TestAddMember extends Test1Skeleton {
	private static final String TEST_DEFS = "testDefs/cute/sourceActions/addTestMember.cpp";
	public TestAddMember(String name) {
		super(name);
 	}
	public final static IAddMemberMethod makeMockObject(String parameter){
		System.out.println("["+parameter +"]");
		String parameters[]=parameter.trim().split(" ");
		StubContainer container=new StubContainer(parameters[0],parameters[2].equals("C")?IAddMemberContainer.ClassType:IAddMemberContainer.InstanceType);
		StubMethod method=new StubMethod(parameters[1],container);
		return method;
	}
	public final static void generateMemberTest(TestSuite ts){
		final ReadTestCase rtc1=new ReadTestCase(TEST_DEFS);
				
		for(int i=0;i<rtc1.testname.size();i++){
			//if(1!=i)continue;
			final int j=i;
			final AddTestMembertoSuiteAction functionAction=new AddTestMembertoSuiteAction();
			functionAction.setUnitTestingMode(makeMockObject(rtc1.parameter.get(j)));
						
			String displayname=rtc1.testname.get(j).replaceAll("[()]", "*");//JUnit unable to display () as name
			junit.framework.TestCase test = new TestAddMember("generateMemberTest"+i+displayname) {
				@Override
				public void runTest() {
					generateTest(rtc1.testname.get(j),rtc1.test.get(j),rtc1.cursorpos.get(j).intValue(),rtc1.expected.get(j),functionAction);
				}
			};
			ts.addTest(test);
		}
	}
}
