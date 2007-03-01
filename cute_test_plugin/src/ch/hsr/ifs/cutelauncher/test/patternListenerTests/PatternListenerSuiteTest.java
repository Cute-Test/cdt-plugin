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
public class PatternListenerSuiteTest extends PatternListenerTest {

	private static final int START_OFFSET_EXP = 14;
	private static final int END_OFFSET_EXP = 42;
	private static final int SUITE_SIZE_EXP = 42;
	private static final String SUITE_NAME = "The Lib Suite";
	
	int startOffset = -1;
	int endLineNo = -1;
	int suiteSize = -1;
	String suiteNameStart;
	String suiteNameEnde;

	private final class ListenerTestHandler extends TestEventHandler{

		@Override
		protected void handleBeginning(IRegion reg, String suitename, String suitesize) {
			startOffset = reg.getOffset();
			suiteNameStart = suitename;
			suiteSize = Integer.parseInt(suitesize);
		}

		@Override
		protected void handleEnding(IRegion reg, String suitename) {
			endLineNo = reg.getOffset();
			suiteNameEnde = suitename;	
		}

		@Override
		protected void handleError(IRegion reg, String testName, String msg) {
//			 do nothing	
		}

		@Override
		protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
//			 do nothing	
		}

		@Override
		public void handleSessionEnd() {
//			 do nothing	
		}

		@Override
		public void handleSessionStart() {
//			 do nothing	
		}

		@Override
		protected void handleSuccess(IRegion reg, String name, String msg) {
//			 do nothing	
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
			// do nothing			
		}
		
	}
	
	public void testSuiteStart() {
		assertEquals("Start Offset", START_OFFSET_EXP, startOffset);
		assertEquals("Suite Name Test", SUITE_NAME, suiteNameStart);
		assertEquals("Suite Size", SUITE_SIZE_EXP, suiteSize);
	}
	
	public void testSuiteEnd() {
		assertEquals("End Offset", END_OFFSET_EXP, endLineNo);
		assertEquals("Suite Name Test", SUITE_NAME, suiteNameEnde);
	}
	
	
	@Override
	protected void addTestEventHandler(CutePatternListener lis) {
		lis.addHandler(new ListenerTestHandler());
	}

	@Override
	protected String getInputFile() {
		return "testDefs/patternListenerTests/suiteTest.txt";
	}

}
