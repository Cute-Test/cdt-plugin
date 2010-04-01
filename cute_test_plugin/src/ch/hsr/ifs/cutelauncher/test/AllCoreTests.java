package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cutelauncher.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;

public class AllCoreTests extends TestSuite{
	
	

	public AllCoreTests() {
		super("Cute Plugin All Core Tests");
		addTest(PatternListenerSuite.suite());
		addTest(ModelBuilderSuite.suite());
//		addTest(SourceLookupPathTest.suite());
	}

	public static Test suite() {
		return new AllCoreTests();
	}

}
//adding just a single test example
//suite.addTest(new DelegateTest("testSourcelookupCustomPath"));
//@see http://mea-bloga.blogspot.com/2007/07/am-i-headless.html
