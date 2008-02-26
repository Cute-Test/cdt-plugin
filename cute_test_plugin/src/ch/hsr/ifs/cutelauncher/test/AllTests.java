package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cutelauncher.test.hyperlinksTests.HyperlinkSuite;
import ch.hsr.ifs.cutelauncher.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All Tests");
		//$JUnit-BEGIN$
		suite.addTest(PatternListenerSuite.suite());
		suite.addTest(HyperlinkSuite.suite()); // TODO need ui-test
		suite.addTest(ModelBuilderSuite.suite());
		suite.addTest(SourceLookupPathTest.suite());
		suite.addTest(SourceActionsTest.suite());
		//requires UI
		suite.addTest(CuteSuiteWizardHandlerTest.suite());
		//$JUnit-END$
		return suite;
	}

}
//adding just a single test example
//suite.addTest(new DelegateTest("testSourcelookupCustomPath"));
