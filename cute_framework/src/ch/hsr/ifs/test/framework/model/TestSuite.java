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

/**
 * @author egraf
 *
 */
public class TestSuite extends TestElement implements ITestComposite, ITestElementListener {
	
	private String name = ""; //$NON-NLS-1$
	
	private int totalTests = 0; 
	private int success = 0;
	private int failure = 0;
	private int error = 0;
	
	private TestStatus status;
	
	private Vector<TestElement> cases = new Vector<TestElement>();
	private Vector<ITestCompositeListener> listeners = new Vector<ITestCompositeListener>();
	
	

	public TestSuite(String name, int totalTests, TestStatus status) {
		super();
		this.name = name;
		this.totalTests = totalTests;
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public TestStatus getStatus() {
		return status;
	}
		
	protected void endTest(TestElement tCase) {
		switch(tCase.getStatus()) {
		case success:
			++success;
			break;
		case failure:
			++failure;
			break;
		case error:
			++error;
			break;
		}
		notifyListeners(new NotifyEvent(NotifyEvent.EventType.testFinished, tCase));
	}
	
	private void setEndStatus() {
		if(cases.size() == 0) {
			status = TestStatus.success;
		}else {
			for (TestElement tCase : cases) {
				switch (status) {
				case running:
					status = tCase.getStatus();
					break;
				case success:
					if(tCase.getStatus() != TestStatus.success) {
						status = tCase.getStatus();
					}
					break;
				case failure:
					if(tCase.getStatus() == TestStatus.error) {
						status = tCase.getStatus();
					}
					break;
				default:
					//nothing
				}
			}
		}
	}

	public int getError() {
		return error;
	}

	public int getFailure() {
		return failure;
	}

	public int getSuccess() {
		return success;
	}

	public int getTotalTests() {
		return totalTests;
	}
	
	public boolean hasErrorOrFailure() {
		return failure + error > 0;
	}

	public int getRun() {
		return success + failure + error;
	}
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @since 3.0
	 */
	public void end(TestCase currentTestCase) {
		if(testsPerformed() != getTotalTests() && currentTestCase != null) {
			currentTestCase.endTest(null, 0, new TestResult("Test ended unexpectedly"), TestStatus.error); //$NON-NLS-1$
		}
		setEndStatus();
		notifyListeners(new NotifyEvent(NotifyEvent.EventType.suiteFinished, this));
	}

	private int testsPerformed() {
		return error + failure + success;
	}

	public void addTestElement(TestElement element) {
		cases.add(element);
		element.setParent(this);
		element.addTestElementListener(this);
		for (ITestCompositeListener lis : listeners) {
			lis.newTestElement(this, element);
		}
	}

	public Vector<TestElement> getElements() {
		return cases;
	}

	public void modelCanged(TestElement source, NotifyEvent event) {
		if(event.getType() == NotifyEvent.EventType.testFinished) {
			endTest(source);
		}
		
	}

	public void addListener(ITestCompositeListener listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(ITestCompositeListener listener) {
		listeners.remove(listener);
	}

}
