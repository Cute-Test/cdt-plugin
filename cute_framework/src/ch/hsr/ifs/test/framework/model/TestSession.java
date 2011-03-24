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

import org.eclipse.debug.core.ILaunch;

public class TestSession implements ITestComposite{
	private Vector<TestElement> rootElements = new Vector<TestElement>();
	private Vector<ITestCompositeListener> listeners = new Vector<ITestCompositeListener>();;
	
	private ILaunch launch;

	public TestSession(ILaunch launch) {
		super();
		this.launch = launch;
	}

	public Vector<TestElement> getRootElements() {
		return rootElements;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public void addTestElement(TestElement element) {
		rootElements.add(element);
		element.setParent(this);
		for (ITestCompositeListener lis : listeners) {
			lis.newTestElement(this, element);
		}
	}

	public Vector<TestElement> getElements() {
		return rootElements;
	}

	public int getError() {
		int tot = 0;
		for (TestElement tElement : rootElements) {
			if (tElement instanceof ITestComposite) {
				ITestComposite testComp = (ITestComposite) tElement;
				tot += testComp.getError();
			}else if (tElement instanceof TestCase) {
				TestCase tCase = (TestCase) tElement;
				if(tCase.getStatus() == TestStatus.error) {
					++tot;
				}
			}
		}
		return tot;
	}

	public int getFailure() {
		int tot = 0;
		for (TestElement tElement : rootElements) {
			if (tElement instanceof ITestComposite) {
				ITestComposite testComp = (ITestComposite) tElement;
				tot += testComp.getFailure();
			}else if (tElement instanceof TestCase) {
				TestCase tCase = (TestCase) tElement;
				if(tCase.getStatus() == TestStatus.failure) {
					++tot;
				}
			}
		}
		return tot;
	}

	public int getRun() {
		int tot = 0;
		for (TestElement tElement : rootElements) {
			if (tElement instanceof ITestComposite) {
				ITestComposite testComp = (ITestComposite) tElement;
				tot += testComp.getRun();
			}else if (tElement instanceof TestCase) {
				TestCase tCase = (TestCase) tElement;
				if(tCase.getStatus() == TestStatus.error ||tCase.getStatus() == TestStatus.failure || tCase.getStatus() == TestStatus.success) {
					++tot;
				}
			}
		}
		return tot;
	}

	public int getSuccess() {
		int tot = 0;
		for (TestElement tElement : rootElements) {
			if (tElement instanceof ITestComposite) {
				ITestComposite testComp = (ITestComposite) tElement;
				tot += testComp.getSuccess();
			}else if (tElement instanceof TestCase) {
				TestCase tCase = (TestCase) tElement;
				if(tCase.getStatus() == TestStatus.success) {
					++tot;
				}
			}
		}
		return tot;
	}

	public int getTotalTests() {
		int tot = 0;
		for (TestElement tElement : rootElements) {
			if (tElement instanceof ITestComposite) {
				ITestComposite testComp = (ITestComposite) tElement;
				tot += testComp.getTotalTests();
			}else if (tElement instanceof TestCase) {
				++tot;
			}
		}
		return tot;
	}

	public boolean hasErrorOrFailure() {
		return getFailure() + getError() > 0;
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