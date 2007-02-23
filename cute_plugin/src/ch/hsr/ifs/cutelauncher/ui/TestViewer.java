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

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
import ch.hsr.ifs.cutelauncher.model.ISessionListener;
import ch.hsr.ifs.cutelauncher.model.ITestComposite;
import ch.hsr.ifs.cutelauncher.model.ITestCompositeListener;
import ch.hsr.ifs.cutelauncher.model.ITestElementListener;
import ch.hsr.ifs.cutelauncher.model.NotifyEvent;
import ch.hsr.ifs.cutelauncher.model.TestCase;
import ch.hsr.ifs.cutelauncher.model.TestElement;
import ch.hsr.ifs.cutelauncher.model.TestSession;
import ch.hsr.ifs.cutelauncher.model.TestStatus;
import ch.hsr.ifs.cutelauncher.model.TestSuite;

public class TestViewer extends Composite implements ITestElementListener, ISessionListener, ITestCompositeListener{
	
	private final class UpdateTestElement extends UIJob {
		private UpdateTestElement(String name, TestElement element, boolean reveal) {
			super(name);
			this.element = element;
			this.reveal = reveal;
		}
		
		private TestElement element;
		private boolean reveal;
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			treeViewer.refresh(element, true);
			if(reveal) {
				treeViewer.reveal(element);
			}
			return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
		}
	}
	
	private final class ShowNewTest extends UIJob {
		private ShowNewTest(String name, ITestComposite composite, TestElement newElement) {
			super(name);
			this.parent = composite;
			this.element = newElement;
		}
		
		private ITestComposite parent;
		private TestElement element;

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			treeViewer.refresh(parent, true);
			if(viewPart.isAutoScroll()){
				treeViewer.reveal(element);
			}
			return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
		}
	}

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
	
	private final class FailuresOnlyFilter extends ViewerFilter{

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof TestElement) {
				TestElement testElement = (TestElement) element;
				switch(testElement.getStatus()) {
				case running:
				case error:
				case failure:
					return true;
				default:
					return false;
				}
			}else {
				return true;
			}
		}
		
	}

	private SashForm sashForm = null;
	private TreeViewer treeViewer = null;
	private TestResultViewer testResultViewer = null;
	
	private TestSession session;
	private Vector<TestElement> elemets = new Vector<TestElement>();
	
	private TestRunnerViewPart viewPart;
	
	private boolean failureOnly = false;
	private FailuresOnlyFilter failuresOnlyFilter = new FailuresOnlyFilter();;
	
	public TestViewer(Composite parent, int style, TestRunnerViewPart viewPart) {
		super(parent, style);
		this.viewPart = viewPart;
		CuteLauncherPlugin.getModel().addListener(this);
		initialize();
		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				CuteLauncherPlugin.getModel().removeListener(TestViewer.this);
			}
		});
	}

	public void reset(TestSession session) {
		testResultViewer.setText("");
		treeViewer.setInput(session);
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
		testResultViewer.setEditable(false);
		testResultViewer.setIndent(5);
	}

	public void modelCanged(TestElement source, NotifyEvent event) {
		UIJob job = null;
		switch(event.getType()) {
		case suiteFinished:
			job = new UpdateTestElement("Show new Test", event.getElement(), false);
			break;
		case testFinished:
			job = new UpdateTestElement("Update Test", event.getElement(), true);
			break;
		}

		if(job != null) {
			job.schedule();
		}
	}

	public void sessionStarted(TestSession session) {
		this.session = session;
		session.addListener(this);
		UIJob job = new UIJob("Reset TestViewer") {
			
			@Override
			public boolean belongsTo(Object family) {
				return CuteLauncherPlugin.PLUGIN_ID.equals(family);
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				reset(TestViewer.this.session);
				return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
			}
			
		};
		job.schedule();
	}
	
	public void sessionFinished(TestSession session) {
		
	}
	
	public void setFailuresOnly(boolean failureOnly) {
		this.failureOnly = failureOnly;
		updateFilters();
	}

	private void updateFilters() {
		if(failureOnly) {
			treeViewer.addFilter(failuresOnlyFilter);
		}else {
			treeViewer.removeFilter(failuresOnlyFilter);
		}
	}

	public void selectNextFailure() {
		if(session.hasErrorOrFailure()) {
			Object firstElement = getSelectedElement();
			if (firstElement instanceof TestCase) {
				TestCase tCase = (TestCase) firstElement;
				treeViewer.setSelection(new StructuredSelection(findNextFailure(tCase)), true);
			}else {
				treeViewer.setSelection(new StructuredSelection(findNextFailure(null)), true);
			}
			
		}
	}

	private Object getSelectedElement() {
		StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
		Object firstElement = selection.getFirstElement();
		return firstElement;
	}

	public void selectFirstFailure() {
		treeViewer.setSelection(new StructuredSelection(findFirstFailure()), true);
	}
	
	private TestElement findFirstFailure() {
		Vector<TestElement> elements = session.getElements();
		for (TestElement element : elements) {
			if(element.getStatus() == TestStatus.failure || element.getStatus() == TestStatus.error) {
				if (element instanceof ITestComposite) {
					ITestComposite composite = (ITestComposite) element;
					return findNextChildFailure(composite);
				}else {
					return element;
				}
			}
		}
		return null;
	}

	private TestElement findNextChildFailure(ITestComposite composite) {
		Vector<? extends TestElement> elements = composite.getElements();
		for (TestElement element : elements) {
			if(element.getStatus() == TestStatus.failure || element.getStatus() == TestStatus.error) {
				if (element instanceof ITestComposite) {
					ITestComposite newComposite = (ITestComposite) element;
					return findNextChildFailure(newComposite);
				}else {
					return element;
				}
			}
		}
		return null;
	}

	public void selectPrevFailure() {
		if(session.hasErrorOrFailure()) {
			Object firstElement = getSelectedElement();
			if (firstElement instanceof TestCase) {
				TestCase tCase = (TestCase) firstElement;
				treeViewer.setSelection(new StructuredSelection(findPrevFailure(tCase)), true);
			}else { //show first Failure
				treeViewer.setSelection(new StructuredSelection(findNextFailure(null)), true);
			}
			
		}
		
	}
	
	private TestElement findPrevFailure(TestCase selected) {
		Vector<TestElement> tests = session.getElements();
		int index = tests.indexOf(selected);
		TestElement prevFailure;
		for(int i = index -1; i >= 0;--i) {
			prevFailure = tests.elementAt(i);
			if(prevFailure.getStatus() == TestStatus.failure || prevFailure.getStatus() == TestStatus.error) {
				return prevFailure;
			}
		}
		return selected;
	}

	private TestElement findNextFailure(TestCase selected) {
		Vector<? extends TestElement> tests = selected.getParent().getElements();
		int index = tests.indexOf(selected);
		TestElement nextFailure;
		for(int i = index + 1; i < tests.size();++i) {
			nextFailure = tests.elementAt(i);
			if(nextFailure.getStatus() == TestStatus.failure || nextFailure.getStatus() == TestStatus.error) {
				return nextFailure;
			}
		}
		return selected;
	}

	public void newTestElement(ITestComposite source, TestElement newElement) {
		newElement.addTestElementListener(this);
		if (newElement instanceof ITestComposite) {
			ITestComposite composite = (ITestComposite) newElement;
			composite.addListener(this);
		}
		elemets.add(newElement);
		UIJob job = new ShowNewTest("Show new Test", newElement.getParent(), newElement);
		job.schedule();		
	}

}
