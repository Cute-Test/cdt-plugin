package ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests.newimplement.NewImplementRefactoringTestSuite;
import ch.hsr.ifs.cute.refactoringpreview.togglefunction.tests.toggle.ToggleRefactoringTestSuite;

public class AllTests extends TestSuite {
	
	public static Test suite() throws Exception {
		return new AllTests();
	}

	public AllTests() throws Exception {
		addTest(ToggleRefactoringTestSuite.suite());
		addTest(NewImplementRefactoringTestSuite.suite());
	}
}
