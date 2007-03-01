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

/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestSuccessTest extends PatternListenerTest {
	
	private static final String TEST_NAME_EXP = "test";
	private static final int OFFSET_START_EXP = 0;
	private static final int OFFSET_END_EXP = 15;
	private static final String MSG_EXP = "OK";
	
	private String testNameStart;
	private int offsetStart = -1;
	private String testNameEnd;
	private String msgEnd;
	private int offsetEnd;
	
	final class TestSuccessHandler extends TestEventHandler{

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
			// Do nothing
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
			testNameEnd = name;
			msgEnd = msg;
			offsetEnd = reg.getLength();
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			testNameStart = testname;
			offsetStart = reg.getOffset();
		}
		
	}

	@Override
	protected void addTestEventHandler(CutePatternListener lis) {
		lis.addHandler(new TestSuccessHandler());
	}

	@Override
	protected String getInputFile() {
		return "testDefs/patternListenerTests/successTest.txt";
	}
	
	public void testTestStart() {
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
		assertEquals("Teststart Offset", OFFSET_START_EXP, offsetStart);
	}
	
	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
		assertEquals("Testend Offset", OFFSET_END_EXP, offsetEnd);
		assertEquals("Message", MSG_EXP, msgEnd);
	}

}
