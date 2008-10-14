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

import ch.hsr.ifs.cute.framework.ConsolePatternListener;
import ch.hsr.ifs.cute.framework.event.TestEventHandler;

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
	
	final class ErrorHandler extends TestEventHandler{

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
			testNameEnd = testName;
			PatternListenerErrorTest.this.msg = msg;
		}

		@Override
		protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
//			Do nothing
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
