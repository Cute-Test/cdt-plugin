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
package ch.hsr.ifs.cutelauncher.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.hsr.ifs.cutelauncher.model.TestSession;
import ch.hsr.ifs.cutelauncher.model.TestSuite;

public class TestRunnerView extends ViewPart {

	public static final String ID = "ch.hsr.ifs.cutelauncher.resultView";

	private Composite top = null;

	private Composite TopPanel = null;

	private CounterPanel counterPanel = null;

	private CuteProgressBar cuteProgressBar = null;

	private TestViewer testViewer = null;

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		top = new Composite(parent, SWT.NONE);
		top.setLayout(gridLayout);
		createTopPanel();
		createTestViewer();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	public boolean isCreated() {
		return counterPanel != null;
	}

	/**
	 * This method initializes TopPSanel	
	 *
	 */
	private void createTopPanel() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		TopPanel = new Composite(top, SWT.NONE);
		createCounterPanel();
		TopPanel.setLayout(gridLayout1);
		TopPanel.setLayoutData(gridData);
		createCuteProgressBar();
	}

	/**
	 * This method initializes counterPanel	
	 *
	 */
	private void createCounterPanel() {
		GridData gridData1 = new org.eclipse.swt.layout.GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		counterPanel = new CounterPanel(TopPanel, SWT.NONE);
		counterPanel.setLayoutData(gridData1);
	}

	/**
	 * This method initializes cuteProgressBar	
	 *
	 */
	private void createCuteProgressBar() {
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.horizontalIndent = 35;
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		cuteProgressBar = new CuteProgressBar(TopPanel);
		cuteProgressBar.setLayoutData(gridData2);
	}

	public void reset() {
		counterPanel.reset();
		cuteProgressBar.reset();
	}
	
	public void reset(TestSession session) {
		reset();
		TestSuite suite = session.getRoot();
		setTotal(suite.getTotalTests(), 0);
		testViewer.reset(session);
	}
	
	public void setTotal(int total, int run) {
		counterPanel.setTotal(total);
		counterPanel.setRun(run);
		cuteProgressBar.reset(false, false, run, total);
	}
	
	public void update(TestSession session) {
		TestSuite suite = session.getRoot();
		counterPanel.setRun(suite.getRun());
		counterPanel.setFailures(suite.getFailure());
		counterPanel.setErrors(suite.getError());
		int failuresAndErrors = suite.getFailure() + suite.getError();
		cuteProgressBar.step(failuresAndErrors);
		cuteProgressBar.refresh(failuresAndErrors > 0);
		testViewer.update(session);
	}
	
	public void addTest(TestSession session	) {
		testViewer.update(session);
	}

	/**
	 * This method initializes testViewer	
	 *
	 */
	private void createTestViewer() {
		GridData gridData3 = new org.eclipse.swt.layout.GridData();
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.grabExcessVerticalSpace = true;
		testViewer = new TestViewer(top, SWT.NONE);
		testViewer.setLayoutData(gridData3);
	}


}  //  @jve:decl-index=0:visual-constraint="148,36,771,201"
