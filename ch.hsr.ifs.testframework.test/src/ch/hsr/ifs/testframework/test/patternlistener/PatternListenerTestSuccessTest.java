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
public class PatternListenerTestSuccessTest extends PatternListenerBase {

	private static final String TEST_NAME_EXP = "xUnitTest";
	private static final String MSG_EXP = "OK";

	private String testNameStart;
	private String testNameEnd;
	private String msgEnd;

	final class TestSuccessHandler extends TestEventHandler {

		@Override
		protected void handleBeginning(IRegion reg, String suitename, String suitesize) {
			// Do nothing
		}

		@Override
		protected void handleEnding(IRegion reg, String suitename) {
			// Do nothing
		}

		@Override
		protected void handleError(IRegion reg, String testName, String msg) {
			// Do nothing
		}

		@Override
		protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
			// Do nothing
		}

		@Override
		public void handleSessionEnd() {
			// Do nothing
		}

		@Override
		public void handleSessionStart() {
			// Do nothing
		}

		@Override
		protected void handleSuccess(IRegion reg, String name, String msg) {
			testNameEnd = name;
			msgEnd = msg;
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			testNameStart = testname;
		}

	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new TestSuccessHandler());
	}

	public void testTestStart() {
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
	}

	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
		assertEquals("Message", MSG_EXP, msgEnd);
	}

	@Override
	protected String getInputFileName() {
		return "successTest.txt";
	}

}
