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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.test.framework.ConsolePatternListener;
import ch.hsr.ifs.test.framework.event.TestEventHandler;

/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerSuiteTest extends PatternListenerBase {
	List<Integer> suiteSize = new ArrayList<Integer>();
	List<String> suiteNameStart = new ArrayList<String>();
	List<String> suiteNameEnded = new ArrayList<String>();

	private final class ListenerTestHandler extends TestEventHandler{

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
	
	public void testFirstStarted() {
		assertEquals("Suite Name Test", "TestSuite1", suiteNameStart.get(0));
		assertEquals("Suite Size", new Integer(42), suiteSize.get(0));
	}
	
	public void testFirstEnded() {
		assertEquals("Suite Name Test", "TestSuite1", suiteNameEnded.get(0));
	}
	
	public void testLastStarted() {
		assertEquals("Suite Name Test", "TestSuite2", suiteNameStart.get(1));
		assertEquals("Suite Size", new Integer(1), suiteSize.get(1));
	}
	
	public void testLastEnded() {
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
