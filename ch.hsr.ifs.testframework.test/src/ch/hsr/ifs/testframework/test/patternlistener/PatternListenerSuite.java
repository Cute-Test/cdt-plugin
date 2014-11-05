/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.patternlistener;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class PatternListenerSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Pattern Listener Suite");
		suite.addTestSuite(PatternListenerSessionStartEndTest.class);
		suite.addTestSuite(PatternListenerSuiteTest.class);
		suite.addTestSuite(PatternListenerTestSuccessTest.class);
		suite.addTestSuite(PatternListenerTestFailedTest.class);
		suite.addTestSuite(PatternListenerTestEqualsFailed.class);
		suite.addTestSuite(PatternListenerErrorTest.class);
		suite.addTestSuite(TestFailureBackslashEscaped.class);
		return suite;
	}
}
