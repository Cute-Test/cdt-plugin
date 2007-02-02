/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule f�r Technik  
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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cutelauncher.model.TestCase;
import ch.hsr.ifs.cutelauncher.model.TestElement;
import ch.hsr.ifs.cutelauncher.model.TestSession;
import ch.hsr.ifs.cutelauncher.model.TestSuite;

public class TestViewer extends Composite {
	
	private class TestResultViewer extends StyledText {
		private class TestResultDClickListener extends MouseAdapter{

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CuteCompareResultAction action = new CuteCompareResultAction(test, TestViewer.this.getShell());
				action.run();
			}

					
		}
		
		TestCase test;

		public TestResultViewer(Composite parent, int style) {
			super(parent, style);
			addMouseListener(new TestResultDClickListener());
		}
		
		public void showTestDetail(TestElement test) {
			if (test instanceof TestCase) {
				TestCase tCase = (TestCase) test;
				this.test = tCase;
				testResultViewer.setText(tCase.getMessage());
				redraw();			
			}else if (test instanceof TestSuite) {
				testResultViewer.setText("");
				redraw();
			}
		}
		
	}

	private SashForm sashForm = null;
	private TreeViewer treeViewer = null;
	private TestResultViewer testResultViewer = null;
	public TestViewer(Composite parent, int style) {
		super(parent, style);
		initialize();
	}
	
	public void reset(TestSession session) {
		testResultViewer.setText("");
		update(session);
	}
	
	public void update(TestSession suite) {
		treeViewer.setInput(suite);
	}
	
	public void showTestDetails(TestElement testElement) {
		testResultViewer.showTestDetail(testElement);
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		createSashForm();
		this.setLayout(gridLayout);
		setSize(new Point(300, 200));
	}

	/**
	 * This method initializes sashForm	
	 *
	 */
	private void createSashForm() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		sashForm = new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(gridData);
		treeViewer = new TreeViewer(sashForm, SWT.FLAT);
		treeViewer.setContentProvider(new CuteTestTreeContentProvieder());
		treeViewer.setLabelProvider(new CuteTestLabelProvider());
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		treeViewer.addSelectionChangedListener(new CuteTestSelecetionListener(this));
		treeViewer.addDoubleClickListener(new CuteTestDClickListener());
		testResultViewer = new TestResultViewer(sashForm, SWT.FLAT);
		testResultViewer.setIndent(5);
	}

}
