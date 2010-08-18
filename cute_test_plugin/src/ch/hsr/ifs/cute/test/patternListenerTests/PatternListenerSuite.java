package ch.hsr.ifs.cute.test.patternListenerTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PatternListenerSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Pattern Listener Suite"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(PatternListenerSessionStartEndTest.class);
		suite.addTestSuite(PatternListenerSuiteTest.class);
		suite.addTestSuite(PatternListenerTestSuccessTest.class);
		suite.addTestSuite(PatternListenerTestFailedTest.class);
		suite.addTestSuite(PatternListenerTestEqualsFailed.class);
		suite.addTestSuite(PatternListenerErrorTest.class);
		suite.addTestSuite(PatternListenerStoryTest.class);
		//$JUnit-END$
		return suite;
	}

}
