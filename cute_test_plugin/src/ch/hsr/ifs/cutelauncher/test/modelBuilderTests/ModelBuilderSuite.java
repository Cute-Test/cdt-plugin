package ch.hsr.ifs.cutelauncher.test.modelBuilderTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ModelBuilderSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for ch.hsr.ifs.cutelauncher.test.modelBuilderTests");
		//$JUnit-BEGIN$
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/sessionTest.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/suiteTest.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/suiteTest2.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/suiteTest3.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/suiteTest4.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/suiteTest5.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/suiteTest6.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/failedTest.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/failedEqualsTest.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/errorTest.txt"));
		suite.addTest(ModelBuilderTest.suite("testDefs/modelBuilderTests/successTest.txt"));
		//$JUnit-END$
		return suite;
	}

}
