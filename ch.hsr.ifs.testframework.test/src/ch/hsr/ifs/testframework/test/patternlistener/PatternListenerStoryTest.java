/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.patternlistener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.event.TestEventHandler;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;

/**
 * @author Mike Bria
 * 
 */
public class PatternListenerStoryTest extends PatternListenerBase {

	int startOffset = -1;
	int endLineNo = -1;

	List<Integer> suiteSize = new ArrayList<Integer>();
	List<String> suiteNameStart = new ArrayList<String>();
	List<String> suiteNameEnded = new ArrayList<String>();

	private final class ListenerTestHandler extends TestEventHandler {

		@Override
		protected void handleBeginning(IRegion reg, String suitename, String suitesize) {
			suiteNameStart.add(suitename);
			suiteSize.add(Integer.parseInt(suitesize));
		}

		@Override
		protected void handleEnding(IRegion reg, String suitename) {
			suiteNameEnded.add(suitename);
		}

		@Override
		protected void handleError(IRegion reg, String testName, String msg) {
			// do nothing
		}

		@Override
		protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
			// do nothing
		}

		@Override
		public void handleSessionEnd() {
			// do nothing
		}

		@Override
		public void handleSessionStart() {
			// do nothing
		}

		@Override
		protected void handleSuccess(IRegion reg, String name, String msg) {
			// do nothing
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			// do nothing
		}

	}

	public void testTODO() throws Exception {

	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new ListenerTestHandler());
	}

	@Override
	protected String getInputFileName() {
		return "storytest.txt";
	}

}
