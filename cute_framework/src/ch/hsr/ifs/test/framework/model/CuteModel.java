/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.test.framework.model;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunch;


public class CuteModel {
	
	private Vector<ISessionListener> sessionListeners = new Vector<ISessionListener>();
	

	private ITestComposite currentParent;


	private TestSession session;

	public void startSession(ILaunch launch) {
		session = new TestSession(launch);
		currentParent = session;
		notifyListenerSessionStart(session);
	}
	
	public void startSuite(TestSuite suite) {
		currentParent.addTestElement(suite);
		currentParent = suite;
	}
	
	public void addTest(TestCase test) {
		if(currentParent != null) {
			currentParent.addTestElement(test);
		}
	}
	
	public void endCurrentTestCase(IFile file, int lineNumber, String msg, TestStatus status, TestCase tCase) {
		TestResult result;
		switch (status) {
		case failure:
			result = new TestFailure(msg);
			break;
		default:
			result = new TestResult(msg);
			break;
		}
		tCase.endTest(file, lineNumber, result, status);
	}

	public void endSuite() {
		if (currentParent instanceof TestSuite) {
			TestSuite suite = (TestSuite) currentParent;
			suite.end(null);
			currentParent = suite.getParent();
		}
		
	}
	
	/**
	 * @since 3.0
	 */
	public void endSession(TestCase currentTestCase) {
		if (currentParent instanceof TestSuite) {
			TestSuite suite = (TestSuite) currentParent;
			suite.end(currentTestCase);
		}
		notifyListenerSessionEnd(session);
	}
	
	public void addListener(ISessionListener lis) {
		if(!sessionListeners.contains(lis)) {
			sessionListeners.add(lis);
		}
	}
	
	public void removeListener(ISessionListener lis) {
		sessionListeners.remove(lis);
	}
	
	private void notifyListenerSessionStart(TestSession session) {
		for (ISessionListener lis : sessionListeners) {
			lis.sessionStarted(session);
		}
	}
	
	private void notifyListenerSessionEnd(TestSession session) {
		for (ISessionListener lis : sessionListeners) {
			lis.sessionFinished(session);
		}
	}
	
	public TestSession getSession() {
		return session;
	}
}
