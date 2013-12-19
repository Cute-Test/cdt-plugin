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

import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.test.mock.DummyTestEventHandler;

/**
 * @author Emanuel Graf
 * 
 */
public class PatternListenerErrorTest extends PatternListenerBase {
	private static final String TEST_NAME_EXP = "xUnitTest";
	private static final String MSG_EXP = "instance of 'std::exception'";

	private String testNameStart;
	private String testNameEnd;
	private String msg;

	final class ErrorHandler extends DummyTestEventHandler {

		@Override
		protected void handleError(IRegion reg, String testName, String msg) {
			testNameEnd = testName;
			PatternListenerErrorTest.this.msg = msg;
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			testNameStart = testname;
		}
	}

	public void testTestStart() {
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
	}

	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
		assertEquals("Message", MSG_EXP, msg);
	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new ErrorHandler());
	}

	@Override
	protected String getInputFileName() {
		return "errorTest.txt";
	}

}
