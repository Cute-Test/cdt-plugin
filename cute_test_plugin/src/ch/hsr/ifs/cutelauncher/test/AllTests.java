package ch.hsr.ifs.cutelauncher.test;

import ch.hsr.ifs.cutelauncher.test.hyperlinksTests.HyperlinkSuite;
import ch.hsr.ifs.cutelauncher.test.modelBuilderTests.ModelBuilderSuite;
import ch.hsr.ifs.cutelauncher.test.patternListenerTests.PatternListenerSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Cute Plugin All Tests");
		//$JUnit-BEGIN$
//		suite.addTest(PatternListenerSuite.suite());
		suite.addTest(HyperlinkSuite.suite());
//		suite.addTest(ModelBuilderSuite.suite());
		//$JUnit-END$
		return suite;
	}

}
