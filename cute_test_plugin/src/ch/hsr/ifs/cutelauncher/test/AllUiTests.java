package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cutelauncher.test.hyperlinksTests.HyperlinkSuite;
import ch.hsr.ifs.cutelauncher.test.ui.sourceactions.SourceActionsTest;

public class AllUiTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All UI Tests");

		suite.addTest(HyperlinkSuite.suite());
		suite.addTest(CuteSuiteWizardHandlerTest.suite());
		suite.addTest(SourceActionsTest.suite());

		return suite;
	}

}
//adding just a single test example
//suite.addTest(new DelegateTest("testSourcelookupCustomPath"));
//@see http://mea-bloga.blogspot.com/2007/07/am-i-headless.html
