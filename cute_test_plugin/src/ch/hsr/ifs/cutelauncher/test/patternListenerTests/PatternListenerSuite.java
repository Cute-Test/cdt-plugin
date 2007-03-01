package ch.hsr.ifs.cutelauncher.test.patternListenerTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PatternListenerSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Patter Listener Suite");
		//$JUnit-BEGIN$
		suite.addTestSuite(PatternListenerSessionStartEndTest.class);
		suite.addTestSuite(PatternListenerSuiteTest.class);
		suite.addTestSuite(PatternListenerTestSuccessTest.class);
		suite.addTestSuite(PatternListenerTestFailedTest.class);
		suite.addTestSuite(PatternListenerTestEqualsFailed.class);
		suite.addTestSuite(PatternListenerErrorTest.class);
		//$JUnit-END$
		return suite;
	}

}
