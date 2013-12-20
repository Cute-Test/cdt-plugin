/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.patternlistener;

import java.io.IOException;

import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.test.PatternListenerBase;
import ch.hsr.ifs.testframework.test.mock.DummyTestEventHandler;

/**
 * @author Emanuel Graf
 * 
 */
public class PatternListenerSessionStartEndTest extends PatternListenerBase {

	boolean sessionStarted = false;
	boolean sessionEnded = false;

	final class SessionStartEndHandler extends DummyTestEventHandler {

		@Override
		public void handleSessionEnd() {
			sessionEnded = true;

		}

		@Override
		public void handleSessionStart() {
			sessionStarted = true;
			tc.removePatternMatchListener(cpl);
		}
	}

	public void testListenerEvents() throws IOException, InterruptedException {
		emulateTestRun();
		assertTrue("No session Start", sessionStarted);
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
