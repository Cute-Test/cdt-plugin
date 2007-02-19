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

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
import ch.hsr.ifs.cutelauncher.ui.TestRunnerViewPart;

public class CuteModel {
	
	private final class ShowResultView extends UIJob{
		public ShowResultView() {
			super("Show Result View");
		}
		
		private TestRunnerViewPart showTestRunnerViewPartInActivePage(TestRunnerViewPart testRunner) {
			IWorkbenchPart activePart= null;
			IWorkbenchPage page= null;
			try {
				if (testRunner != null && testRunner.isCreated())
					return testRunner;
				page= CuteLauncherPlugin.getActivePage();
				if (page == null)
					return null;
				activePart= page.getActivePart();

				return (TestRunnerViewPart) page.showView(TestRunnerViewPart.ID);
			} catch (PartInitException pie) {
				CuteLauncherPlugin.log(pie);
				return null;
			} finally{
				//restore focus stolen by the creation of the result view
				if (page != null && activePart != null)
					page.activate(activePart);
			}
		}

		private TestRunnerViewPart findTestRunnerViewPartInActivePage() {
			IWorkbenchPage page= CuteLauncherPlugin.getActivePage();
			if (page == null)
				return null;
			return (TestRunnerViewPart) page.findView(TestRunnerViewPart.ID);
		}
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage()) == null) {
				return new Status(IStatus.WARNING, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"Could not show TestResultView",null);
			}else {
				return new Status(IStatus.OK, CuteLauncherPlugin.PLUGIN_ID, IStatus.OK,"OK",null);
			}
		}
		
	}
	
	private Vector<ISessionListener> sessionListeners = new Vector<ISessionListener>();
	

	private TestSuite root;

	public void startNewRun(TestSuite root) {
		TestSession session = new TestSession(root);
		this.root = root;
		UIJob job = new ShowResultView();
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
		}
		notifyListener(session);
	}
	
	public void addTest(TestCase test) {
		root.add(test);
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
		root.end();
	}
	
	public void addListener(ISessionListener lis) {
		if(!sessionListeners.contains(lis)) {
			sessionListeners.add(lis);
		}
	}
	
	public void removeListener(ISessionListener lis) {
		sessionListeners.remove(lis);
	}
	
	private void notifyListener(TestSession session) {
		for (ISessionListener lis : sessionListeners) {
			lis.sessionStarted(session);
		}
	}

}
