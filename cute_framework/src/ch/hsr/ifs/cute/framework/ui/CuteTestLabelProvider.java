/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.framework.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.cute.framework.model.TestCase;
import ch.hsr.ifs.cute.framework.model.TestElement;
import ch.hsr.ifs.cute.framework.model.TestSuite;

/**
 * @author egraf
 *
 */
public class CuteTestLabelProvider  extends LabelProvider {
	
	private Image suiteRun = CuteFrameworkPlugin.getImageDescriptor("obj16/tsuiterun.gif").createImage(); //$NON-NLS-1$
	private Image suiteOk = CuteFrameworkPlugin.getImageDescriptor("obj16/tsuiteok.gif").createImage(); //$NON-NLS-1$
	private Image suiteFail = CuteFrameworkPlugin.getImageDescriptor("obj16/tsuitefail.gif").createImage(); //$NON-NLS-1$
	private Image suiteError = CuteFrameworkPlugin.getImageDescriptor("obj16/tsuiteerror.gif").createImage(); //$NON-NLS-1$
	
	private Image testRun = CuteFrameworkPlugin.getImageDescriptor("obj16/testrun.gif").createImage(); //$NON-NLS-1$
	private Image testOk = CuteFrameworkPlugin.getImageDescriptor("obj16/testok.gif").createImage(); //$NON-NLS-1$
	private Image testFail = CuteFrameworkPlugin.getImageDescriptor("obj16/testfail.gif").createImage(); //$NON-NLS-1$
	private Image testError = CuteFrameworkPlugin.getImageDescriptor("obj16/testerr.gif").createImage(); //$NON-NLS-1$

	public Image getImage(Object element) {
		if (element instanceof TestSuite) {
			TestElement suite = (TestElement) element;
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
				throw new IllegalArgumentException(String.valueOf(element));
			}
		}else if (element instanceof TestCase) {
			TestCase tCase = (TestCase) element;
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
				throw new IllegalArgumentException(String.valueOf(element));
			}
		}else {
			throw new IllegalArgumentException(String.valueOf(element));
		}
	}

	public String getText(Object element) {
		return element.toString();
	}

	public void dispose() {
		// TODO Images disposen

	}
}
