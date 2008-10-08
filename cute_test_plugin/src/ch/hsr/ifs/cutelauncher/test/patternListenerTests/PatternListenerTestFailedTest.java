/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf & Guido Zgraggen- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.test.patternListenerTests;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.cute.core.ConsolePatternListener;
import ch.hsr.ifs.cute.core.event.TestEventHandler;

/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestFailedTest extends PatternListenerBase {
	
	private static final String TEST_NAME_EXP = "xUnitTest";
	private static final String MSG_EXP = "evaluated: `Factorial(-10) < 0`, expected: <true> but was: <false>";
	private static final Object TEST_FILE_NAME_EXP = "../src/sample1_unittest.cc";
	private static final int LINE_NO_EXP = 84;
	
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
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
	}
	
	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
		assertEquals("Message", MSG_EXP, msg);
		assertEquals("Filename", TEST_FILE_NAME_EXP, testFileName);
		assertEquals("Line", LINE_NO_EXP, lineNr);
	}

	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new TestFailedHandler());
	}

	@Override
	protected String getInputFileName() {
		return "failedTest.txt";
	}

}
