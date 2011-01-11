package ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests.newimplement;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests.ExternalRefactoringTester;

public class NewImplementRefactoringTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new NewImplementRefactoringTestSuite();
		suite.addTest(ExternalRefactoringTester.suite("NewImplementRefactoringTest", "resources/refactoring/NewImplementRefactoring.rts")); //$NON-NLS-1$ //$NON-NLS-2$
		return suite;
	}

}
