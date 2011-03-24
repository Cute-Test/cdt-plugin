/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
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
