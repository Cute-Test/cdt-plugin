package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.TestSuite;
import ch.hsr.ifs.cute.ui.sourceactions.AddTestFunctiontoSuiteAction;

public class TestAddFunction extends Test1Skeleton {
	private static final String TEST_DEFS = "testDefs/cute/sourceActions/addTestfunction.cpp";

	public TestAddFunction(String name) {
		super(name);
 	}
	public final void testAddTestFunctionAll(){
		ReadTestCase rtc=new ReadTestCase(TEST_DEFS);
		AddTestFunctiontoSuiteAction functionAction=new AddTestFunctiontoSuiteAction();
		for(int i=0;i<rtc.testname.size();i++){
			generateTest(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i),functionAction);
		}
	}
	
	public final static TestSuite generateFunctionTest(){
		TestSuite functorTS=new TestSuite("addTestFunction Tests");
		final ReadTestCase rtc1=new ReadTestCase(TEST_DEFS);
		final AddTestFunctiontoSuiteAction functionAction=new AddTestFunctiontoSuiteAction();
		for(int i=0;i<rtc1.testname.size();i++){
			final int j=i;
			String displayname=rtc1.testname.get(j).replaceAll("[()]", "*");//JUnit unable to display () as name
			junit.framework.TestCase test = new TestAddFunction("addTestFunction"+i+displayname) {
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
		if(speedupMode){
			TestSuite functorTS=new TestSuite("addTestFunction Tests");
			functorTS.addTest(new TestAddFunction("testAddTestFunctionAll"));
			return functorTS;
		}else{
			return generateFunctionTest();
		}
	}
}
