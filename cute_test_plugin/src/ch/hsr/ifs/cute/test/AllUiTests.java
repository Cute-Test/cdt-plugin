package ch.hsr.ifs.cute.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.test.hyperlinksTests.HyperlinkSuite;
import ch.hsr.ifs.cute.test.ui.checkers.UnregisteredTestFunctionCheckerTest;
import ch.hsr.ifs.cute.test.ui.sourceactions.SourceActionsTest;

public class AllUiTests extends TestSuite{

	public AllUiTests() {
		super("Cute Plugin All UI Tests"); //$NON-NLS-1$
		addTest(HyperlinkSuite.suite());
		addTest(CuteSuiteWizardHandlerTest.suite());
		addTest(SourceActionsTest.suite());
		addTestSuite(UnregisteredTestFunctionCheckerTest.class);
	}
	
	public static Test suite() throws Exception {
		return new AllUiTests();
	}

}
