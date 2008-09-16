package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cutelauncher.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;

public class AllCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All Core Tests");
		//$JUnit-BEGIN$
		suite.addTest(PatternListenerSuite.suite());
		suite.addTest(ModelBuilderSuite.suite());
		suite.addTest(SourceLookupPathTest.suite());

		//$JUnit-END$
		return suite;
	}

}
//adding just a single test example
//suite.addTest(new DelegateTest("testSourcelookupCustomPath"));
//@see http://mea-bloga.blogspot.com/2007/07/am-i-headless.html
