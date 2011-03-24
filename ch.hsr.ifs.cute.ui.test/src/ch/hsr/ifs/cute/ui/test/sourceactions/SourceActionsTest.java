/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.sourceactions;

import junit.framework.Test;
import junit.framework.TestSuite;
public class SourceActionsTest {

	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.sourceactions"); //$NON-NLS-1$

		ts.addTest(TestBugFixes.suite());
//		ts.addTestSuite(AddTestToSuiteTest.class);
//		ts.addTestSuite(NewTestFunctionTest.class);
		
		return ts;
	}
	
}

