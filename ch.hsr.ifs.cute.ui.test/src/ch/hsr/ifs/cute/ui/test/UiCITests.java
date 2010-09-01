package ch.hsr.ifs.cute.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.ui.test.checkers.UnregisteredTestFunctionCheckerTest;

public class UiCITests extends TestSuite{

	public UiCITests() {
		super("Cute Plugin UI Build Server Tests"); //$NON-NLS-1$
		addTest(CuteSuiteWizardHandlerTest.suite());
//		addTest(SourceActionsTest.suite()); //Don'r run on the build server
		addTestSuite(UnregisteredTestFunctionCheckerTest.class);
	}
	
	public static Test suite() throws Exception {
		return new UiCITests();
	}

}
