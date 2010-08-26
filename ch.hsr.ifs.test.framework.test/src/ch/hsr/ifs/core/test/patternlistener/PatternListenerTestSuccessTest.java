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
package ch.hsr.ifs.core.test.patternlistener;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.test.framework.event.TestEventHandler;
import ch.hsr.ifs.test.framework.launch.ConsolePatternListener;

/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestSuccessTest extends PatternListenerBase {
	
	private static final String TEST_NAME_EXP = "xUnitTest"; //$NON-NLS-1$
	private static final String MSG_EXP = "OK"; //$NON-NLS-1$
	
	private String testNameStart;
	private String testNameEnd;
	private String msgEnd;
	
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
		assertEquals("Teststart name", TEST_NAME_EXP, testNameStart); //$NON-NLS-1$
	}
	
	public void testTestEnd() {
		assertEquals("Testend name", TEST_NAME_EXP, testNameEnd); //$NON-NLS-1$
		assertEquals("Message", MSG_EXP, msgEnd); //$NON-NLS-1$
	}
	
	@Override
	protected String getInputFileName() {
		return "successTest.txt"; //$NON-NLS-1$
	}

}
