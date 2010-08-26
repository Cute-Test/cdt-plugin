package ch.hsr.ifs.cute.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.ui.test.checkers.UnregisteredTestFunctionCheckerTest;
import ch.hsr.ifs.cute.ui.test.sourceactions.SourceActionsTest;

public class AllUiTests extends TestSuite{

	public AllUiTests() {
		super("Cute Plugin All UI Tests"); //$NON-NLS-1$
		addTest(CuteSuiteWizardHandlerTest.suite());
		addTest(SourceActionsTest.suite());
		addTestSuite(UnregisteredTestFunctionCheckerTest.class);
	}
	
	public static Test suite() throws Exception {
		return new AllUiTests();
	}

}
