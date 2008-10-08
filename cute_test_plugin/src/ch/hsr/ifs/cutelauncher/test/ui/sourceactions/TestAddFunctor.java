package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.TestSuite;
import ch.hsr.ifs.cute.ui.sourceactions.AddTestFunctortoSuiteAction;

public class TestAddFunctor extends Test1Skeleton {
	private static final String TEST_DEFS = "testDefs/cute/sourceActions/addTestfunctor.cpp";

	public TestAddFunctor(String name) {
		super(name);
 	}
	
	public final static TestSuite generateFunctorTest(){
		TestSuite functorTS=new TestSuite("addTestFunctor Tests");
		final ReadTestCase rtc1=new ReadTestCase(TEST_DEFS);
		final AddTestFunctortoSuiteAction functionAction=new AddTestFunctortoSuiteAction();
		for(int i=0;i<rtc1.testname.size();i++){
			final int j=i;
			String displayname=rtc1.testname.get(j).replaceAll("[()]", "*");//JUnit unable to display () as name
			junit.framework.TestCase test = new TestAddFunctor("generateFunctorTest"+i+displayname) {
				@Override
				public void runTest() {
					generateTest(rtc1.testname.get(j),rtc1.test.get(j),rtc1.cursorpos.get(j).intValue(),rtc1.expected.get(j),functionAction);
				}
			};
			functorTS.addTest(test);
		}
		return functorTS;
	}
	
	public static TestSuite suite(boolean speedupMode){
		return generateFunctorTest();
	}

}
