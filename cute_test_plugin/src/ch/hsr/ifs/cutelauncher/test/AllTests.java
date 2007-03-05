package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cutelauncher.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All Tests");
		//$JUnit-BEGIN$
		suite.addTest(PatternListenerSuite.suite());
//		suite.addTest(HyperlinkSuite.suite()); // TODO need ui-test
		suite.addTest(ModelBuilderSuite.suite());
		//$JUnit-END$
		return suite;
	}

}
