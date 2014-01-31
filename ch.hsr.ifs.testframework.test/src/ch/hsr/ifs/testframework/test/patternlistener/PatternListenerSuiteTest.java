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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.test.PatternListenerBase;
import ch.hsr.ifs.testframework.test.mock.DummyTestEventHandler;

/**
 * @author Emanuel Graf
 * 
 */
public class PatternListenerSuiteTest extends PatternListenerBase {
	List<Integer> suiteSize = new ArrayList<Integer>();
	List<String> suiteNameStart = new ArrayList<String>();
	List<String> suiteNameEnded = new ArrayList<String>();

	private final class ListenerTestHandler extends DummyTestEventHandler {

		@Override
		protected void handleBeginning(IRegion reg, String suitename, String suitesize) {
			suiteNameStart.add(suitename);
			suiteSize.add(Integer.parseInt(suitesize));
		}

		@Override
		protected void handleEnding(IRegion reg, String suitename) {
			suiteNameEnded.add(suitename);
		}
	}

	public void testListenerEvents() throws IOException, InterruptedException {
		emulateTestRun();
		assertListSize(2);
		assertFirstStarted(42);
		assertFirstEnded();
		assertLastStarted(1);
		assertLastEnded();
	}

	private void assertListSize(int expectedListsSize) {
		assertEquals(expectedListsSize, suiteSize.size());
		assertEquals(expectedListsSize, suiteNameStart.size());
		assertEquals(expectedListsSize, suiteNameEnded.size());
	}

	private void assertFirstStarted(int expectedSuiteSize) {
		assertEquals("Suite Name Test", "TestSuite1", suiteNameStart.get(0));
		assertEquals("Suite Size", new Integer(expectedSuiteSize), suiteSize.get(0));
	}

	private void assertFirstEnded() {
		assertEquals("Suite Name Test", "TestSuite1", suiteNameEnded.get(0));
	}

	private void assertLastStarted(int expectedSuiteSize) {
		assertEquals("Suite Name Test", "TestSuite2", suiteNameStart.get(1));
		assertEquals("Suite Size", new Integer(expectedSuiteSize), suiteSize.get(1));
	}

	private void assertLastEnded() {
		assertEquals("Suite Name Test", "TestSuite2", suiteNameEnded.get(1));
	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new ListenerTestHandler());
	}

	@Override
	protected String getInputFileName() {
		return "suiteTest.txt";
	}
}
