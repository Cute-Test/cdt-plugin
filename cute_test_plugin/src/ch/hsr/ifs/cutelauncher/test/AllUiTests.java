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
