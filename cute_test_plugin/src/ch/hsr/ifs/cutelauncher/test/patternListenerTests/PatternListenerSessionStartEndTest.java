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
public class PatternListenerSessionStartEndTest extends PatternListenerTest {

	boolean sessionStarted = false;
	boolean sessionEnded = false;
	
	
	final class SessionStartEndHandler extends TestEventHandler{

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
			sessionEnded = true;
			
		}

		@Override
		public void handleSessionStart() {
			sessionStarted = true;
			tc.removePatternMatchListener(cpl);
		}

		@Override
		protected void handleSuccess(IRegion reg, String name, String msg) {
//			 Do nothing
		}

		@Override
		protected void handleTestStart(IRegion reg, String testname) {
//			 Do nothing
		}
		
	}

	public void testSessionStart() {
		assertTrue("No session Start", sessionStarted);
	}
	
	public void testSessionEnd() {
		assertTrue("No session End", sessionEnded);
	}

	@Override
	protected void addTestEventHandler(CutePatternListener lis) {
		lis.addHandler(new SessionStartEndHandler());
	}

	@Override
	protected String getInputFile() {
		return "testDefs/patternListenerTests/sessionTest.txt";
	}
	
	

}