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

import ch.hsr.ifs.cutelauncher.CutePatternListener;
import ch.hsr.ifs.cutelauncher.TestEventHandler;
import ch.hsr.ifs.cutelauncher.test.ConsoleTest;

/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestFailedTest extends ConsoleTest {
	
	private static final String TEST_NAME_EXP = "test";
	private static final int OFFSET_START_EXP = 0;
	private static final int OFFSET_END_EXP = 15;
	private static final String MSG_EXP = "false";
	private static final Object TEST_FILE_NAME_EXP = "../src/Test.cpp";
	private static final int LINE_NO_EXP = 7;
	
	private String testNameStart;
	private int offsetStart = -1;
	private String testNameEnd;
	private String msg;
	private int offsetEnd;
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
			offsetEnd = reg.getOffset();
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
			offsetStart = reg.getOffset();
		}
		
	}
	
	public void testTestStart() {
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
		assertEquals("Teststart Offset", OFFSET_START_EXP, offsetStart);
	}
	
	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
		assertEquals("Testend Offset", OFFSET_END_EXP, offsetEnd);
		assertEquals("Message", MSG_EXP, msg);
		assertEquals("Filename", TEST_FILE_NAME_EXP, testFileName);
		assertEquals("Line", LINE_NO_EXP, lineNr);
	}

	@Override
	protected void addTestEventHandler(CutePatternListener lis) {
		lis.addHandler(new TestFailedHandler());
	}

	@Override
	protected String getInputFile() {
		return "testDefs/patternListenerTests/failedTest.txt";
	}

}
