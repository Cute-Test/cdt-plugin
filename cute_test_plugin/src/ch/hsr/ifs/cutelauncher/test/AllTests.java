package ch.hsr.ifs.cutelauncher.test;

import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All Tests");
		//$JUnit-BEGIN$
		suite.addTest(PatternListenerSuite.suite());
		//$JUnit-END$
		return suite;
	}

}
