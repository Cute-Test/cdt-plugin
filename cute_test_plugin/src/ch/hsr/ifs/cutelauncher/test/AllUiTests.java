package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cutelauncher.test.hyperlinksTests.HyperlinkSuite;
import ch.hsr.ifs.cutelauncher.test.ui.sourceactions.SourceActionsTest;

public class AllUiTests extends TestSuite{

	public AllUiTests() {
		super("Cute Plugin All UI Tests");
		addTest(HyperlinkSuite.suite());
		addTest(CuteSuiteWizardHandlerTest.suite());
		addTest(SourceActionsTest.suite());
	}
	
	public static Test suite() throws Exception {
		return new AllUiTests();
	}

}
