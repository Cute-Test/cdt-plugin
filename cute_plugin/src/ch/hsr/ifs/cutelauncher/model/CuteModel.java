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
package ch.hsr.ifs.cutelauncher.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
import ch.hsr.ifs.cutelauncher.ui.TestRunnerView;

public class CuteModel {
	
	private final class ResetJob extends UIJob {
		TestSession session;
		
		public ResetJob(TestSession session) {
			super("CuteUpdater");
			this.session = session;
		}
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			runnerView = showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage());
			if(runnerView != null) {
				runnerView.reset(session);
			}
			return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
		}
	}
	
	private final class UpdateJob extends UIJob{
		
		TestSession session;
		IFile file;
		int lineNumber;
		String msg;
		TestStatus status;
		TestCase tCase;
		
		public UpdateJob(TestSession session, IFile file, int lineNumber, String msg, TestStatus status, TestCase tCase) {
			super("UpdateJob");
			this.session = session;
			this.file = file;
			this.lineNumber = lineNumber;
			this.msg = msg;
			this.status = status;
			this.tCase = tCase;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			tCase.endTest(file, lineNumber, msg, status);
			runnerView = showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage());
			if(runnerView != null) {
				runnerView.update(session);
			}
			return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
		}

	}

	private final class AddTestJob extends UIJob{

		TestSession session;

		public AddTestJob(TestSession session) {
			super("AddTestJob");
			this.session = session;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			runnerView = showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage());
			if(runnerView != null) {
				runnerView.addTest(session);
			}
			return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
		}

	}
	
	private final class EndSuiteJob extends UIJob{

		TestSession session;

		public EndSuiteJob(TestSession session) {
			super("EndSuiteJob");
			this.session = session;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			session.getRoot().end();
			runnerView = showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage());
			if(runnerView != null) {
				runnerView.addTest(session);
			}
			return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
		}

	}

	private TestSuite root;
	private TestRunnerView runnerView;

	public void startNewRun(TestSuite root) {
		TestSession session = new TestSession(root);
		ResetJob job = new ResetJob(session);
		job.schedule();
		this.root = root;

	}
	
	public void addTest(TestCase test) {
		root.add(test);
		TestSession session = new TestSession(root);
		new AddTestJob(session).schedule();
	}
	
	private TestRunnerView showTestRunnerViewPartInActivePage(TestRunnerView testRunner) {
		IWorkbenchPart activePart= null;
		IWorkbenchPage page= null;
		try {
			if (testRunner != null && testRunner.isCreated())
				return testRunner;
			page= CuteLauncherPlugin.getActivePage();
			if (page == null)
				return null;
			activePart= page.getActivePart();

			return (TestRunnerView) page.showView(TestRunnerView.ID);
		} catch (PartInitException pie) {
			CuteLauncherPlugin.log(pie);
			return null;
		} finally{
			//restore focus stolen by the creation of the result view
			if (page != null && activePart != null)
				page.activate(activePart);
		}
	}

	private TestRunnerView findTestRunnerViewPartInActivePage() {
		IWorkbenchPage page= CuteLauncherPlugin.getActivePage();
		if (page == null)
			return null;
		return (TestRunnerView) page.findView(TestRunnerView.ID);
	}
	
	public void endCurrentTestCase(IFile file, int lineNumber, String msg, TestStatus status, TestCase tCase) {
		TestSession session = new TestSession(root);
		new UpdateJob(session, file, lineNumber, msg, status, tCase).schedule();
	}

	public void endSuite() {
		TestSession session = new TestSession(root);
		new EndSuiteJob(session).schedule();
	}

}
