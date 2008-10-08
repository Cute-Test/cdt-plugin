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

import ch.hsr.ifs.cutelauncher.ConsolePatternListener;
import ch.hsr.ifs.cutelauncher.event.TestEventHandler;

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
	
	public void testTODO() throws Exception {
		
	}
	
//	public void testFirstStarted() {
//		assertEquals("Suite Name Test", "xUnitTest1", suiteNameStart.get(0));
//		assertEquals("Suite Size", new Integer(42), suiteSize.get(0));
//	}
//	
//	public void testFirstEnded() {
//		assertEquals("Suite Name Test", "xUnitTest1", suiteNameEnded.get(0));
//	}
//	
//	public void testLastStarted() {
//		assertEquals("Suite Name Test", "xUnitTest2", suiteNameStart.get(1));
//		assertEquals("Suite Size", new Integer(6), suiteSize.get(1));
//	}
//	
//	public void testLastEnded() {
//		assertEquals("Suite Name Test", "xUnitTest2", suiteNameEnded.get(1));
//	}
	
	
	@Override
	protected void addTestEventHandler(ConsolePatternListener lis) {
		lis.addHandler(new ListenerTestHandler());
	}

	@Override
	protected String getInputFileName() {
		return "storytest.txt";
	}

}
