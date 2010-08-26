package ch.hsr.ifs.core.test.modelbuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ModelBuilderSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for ch.hsr.ifs.cutelauncher.test.modelBuilderTests"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTest(ModelBuilderTest.suite("sessionTest.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("suiteTest.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("suiteTest2.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("suiteTest3.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("suiteTest4.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("suiteTest5.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("suiteTest6.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("failedTest.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("failedEqualsTest.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("errorTest.txt")); //$NON-NLS-1$
		suite.addTest(ModelBuilderTest.suite("successTest.txt")); //$NON-NLS-1$
		//$JUnit-END$
		return suite;
	}

}
