/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.model.TestCase;
import ch.hsr.ifs.testframework.model.TestElement;
import ch.hsr.ifs.testframework.model.TestSuite;

/**
 * @author egraf
 *
 */
public class CuteTestLabelProvider  extends LabelProvider {
	
	private Image suiteRun = TestFrameworkPlugin.getImageDescriptor("obj16/tsuiterun.gif").createImage(); //$NON-NLS-1$
	private Image suiteOk = TestFrameworkPlugin.getImageDescriptor("obj16/tsuiteok.gif").createImage(); //$NON-NLS-1$
	private Image suiteFail = TestFrameworkPlugin.getImageDescriptor("obj16/tsuitefail.gif").createImage(); //$NON-NLS-1$
	private Image suiteError = TestFrameworkPlugin.getImageDescriptor("obj16/tsuiteerror.gif").createImage(); //$NON-NLS-1$
	
	private Image testRun = TestFrameworkPlugin.getImageDescriptor("obj16/testrun.gif").createImage(); //$NON-NLS-1$
	private Image testOk = TestFrameworkPlugin.getImageDescriptor("obj16/testok.gif").createImage(); //$NON-NLS-1$
	private Image testFail = TestFrameworkPlugin.getImageDescriptor("obj16/testfail.gif").createImage(); //$NON-NLS-1$
	private Image testError = TestFrameworkPlugin.getImageDescriptor("obj16/testerr.gif").createImage(); //$NON-NLS-1$

	public Image getImage(Object element) {
		if (element instanceof TestSuite) {
			TestElement suite = (TestElement) element;
			return getSuiteImage(suite);
		}else if (element instanceof TestCase) {
			TestCase tCase = (TestCase) element;
			return getTestCaseImage(tCase);
		}else {
			throw new IllegalArgumentException(String.valueOf(element));
		}
	}

	/**
	 * @since 3.0
	 */
	protected Image getTestCaseImage(TestCase tCase) {
		switch (tCase.getStatus()) {
		case running:
			return testRun;
		case success:
			return testOk;
		case failure:
			return testFail;
		case error:
			return testError;
		default:
			throw new IllegalArgumentException(String.valueOf(tCase));
		}
	}

	/**
	 * @since 3.0
	 */
	protected Image getSuiteImage(TestElement suite) {
		switch (suite.getStatus()) {
		case running:
			return suiteRun;
		case success:
			return suiteOk;
		case failure:
			return suiteFail;
		case error:
			return suiteError;
		default:
			throw new IllegalArgumentException(String.valueOf(suite));
		}
	}

	public String getText(Object element) {
		return element.toString();
	}

	public void dispose() {
		// TODO Images disposen

	}
}
