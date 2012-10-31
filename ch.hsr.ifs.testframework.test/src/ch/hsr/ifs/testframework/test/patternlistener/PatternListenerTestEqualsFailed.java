/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.patternlistener;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.event.TestEventHandler;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;

/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestEqualsFailed extends PatternListenerBase {
	
	private static final String TEST_NAME_EXP = "xUnitTest"; //$NON-NLS-1$
	private static final String MSG_EXP = "evaluated: `Factorial(0)`, expected: <3> but was: <1>"; //$NON-NLS-1$
	private static final Object TEST_FILE_NAME_EXP = "../src/sample1_unittest.cc"; //$NON-NLS-1$
	private static final int LINE_NO_EXP = 104;
	
	private String testNameStart;
	private String testNameEnd;
	private String msg;
	private String testFileName;
	private int lineNr;
	
	final class TestFailedHandler extends TestEventHandler{

		@Override
		protected void handleBeginning(IRegion reg, String suitename, String suitesize) {
//			 Do nothing
		}

		@Override
		protected void handleEnding(IRegion reg, String suitename) {
//			 Do nothing
		}

		@Override
		protected void handleError(IRegion reg, String testName, String msg) {
//			 Do nothing
		}

		@Override
		protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
			testNameEnd = testName;
			testFileName = fileName;
			lineNr = Integer.parseInt(lineNo);
			msg = reason;
		}

		@Override
		public void handleSessionEnd() {
//			 Do nothing
		}

		@Override
		public void handleSessionStart() {
//			 Do nothing
		}

		@Override
		protected void handleSuccess(IRegion reg, String name, String msg) {
//			 Do nothing
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			testNameStart = testname;
		}
		
	}
	public void testTestStart() {
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart); //$NON-NLS-1$
	}
	
	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd); //$NON-NLS-1$
		assertEquals("Message", MSG_EXP, msg); //$NON-NLS-1$
		assertEquals("Filename", TEST_FILE_NAME_EXP, testFileName); //$NON-NLS-1$
		assertEquals("Line", LINE_NO_EXP, lineNr); //$NON-NLS-1$
	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new TestFailedHandler());
	}

	@Override
	protected String getInputFileName() {
		return "failedEqualsTest.txt"; //$NON-NLS-1$
	}

}
