package ch.hsr.ifs.cute.refactoringPreview.clonewar.test;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration.TestConfiguration;

/**
 * Merging all junit tests into a suite.
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("nls")
public class AllTests {
	
	/**
	 * Return all tests.
	 * @return Test suite.
	 * @throws Exception From {@link RefactoringTester}.
	 */
	public static Test suite() throws Exception{
		TestConfiguration config = new TestConfiguration("testconfig/test.xml");
		List<Test> allTests = config.getAllTests();
		TestSuite suite = new TestSuite("CloneWar TestSuite");
		for(Test test : allTests)
			suite.addTest(test);

		return suite;
	}
}
