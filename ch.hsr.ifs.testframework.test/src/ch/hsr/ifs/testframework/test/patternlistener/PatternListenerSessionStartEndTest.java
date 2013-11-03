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
public class PatternListenerSessionStartEndTest extends PatternListenerBase {

	boolean sessionStarted = false;
	boolean sessionEnded = false;

	final class SessionStartEndHandler extends TestEventHandler {

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
			sessionEnded = true;

		}

		@Override
		public void handleSessionStart() {
			sessionStarted = true;
			tc.removePatternMatchListener(cpl);
		}

		@Override
		protected void handleSuccess(IRegion reg, String name, String msg) {
			// Do nothing
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			// Do nothing
		}

	}

	public void testSessionStart() {
		assertTrue("No session Start", sessionStarted);
	}

	public void testSessionEnd() {
		assertTrue("No session End", sessionEnded);
	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new SessionStartEndHandler());
	}

	@Override
	protected String getInputFileName() {
		return "sessionTest.txt";
	}

}
