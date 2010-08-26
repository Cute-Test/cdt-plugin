package ch.hsr.ifs.core.test.hyperlink;

import junit.framework.Test;
import junit.framework.TestSuite;

public class HyperlinkSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Hyperlink Suite"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(HyperlinkTest.class);
		//$JUnit-END$
		return suite;
	}

}
