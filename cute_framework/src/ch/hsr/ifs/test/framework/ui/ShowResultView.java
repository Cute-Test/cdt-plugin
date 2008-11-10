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
package ch.hsr.ifs.test.framework.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.test.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.test.framework.Messages;

public class ShowResultView extends UIJob{
	
	private static Messages msg = CuteFrameworkPlugin.getMessages();

	public ShowResultView() {
		super(msg.getString("ShowResultView.ShowResultView")); //$NON-NLS-1$
	}
	
	private TestRunnerViewPart showTestRunnerViewPartInActivePage(TestRunnerViewPart testRunner) {
		IWorkbenchPart activePart= null;
		IWorkbenchPage page= null;
		try {
			if (testRunner != null && testRunner.isCreated())
				return testRunner;
			page= CuteFrameworkPlugin.getActivePage();
			if (page == null)
				return null;
			activePart= page.getActivePart();

			return (TestRunnerViewPart) page.showView(TestRunnerViewPart.ID);
		} catch (PartInitException pie) {
			CuteFrameworkPlugin.log(pie);
			return null;
		} finally{
			//restore focus stolen by the creation of the result view
			if (page != null && activePart != null)
				page.activate(activePart);
		}
	}

	private TestRunnerViewPart findTestRunnerViewPartInActivePage() {
		IWorkbenchPage page= CuteFrameworkPlugin.getActivePage();
		if (page == null)
			return null;
		return (TestRunnerViewPart) page.findView(TestRunnerViewPart.ID);
	}
	
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage()) == null) {
			return new Status(IStatus.WARNING, CuteFrameworkPlugin.PLUGIN_ID, IStatus.OK,msg.getString("ShowResultView.CouldNotShowResultView"),null); //$NON-NLS-1$
		}else {
			return new Status(IStatus.OK, CuteFrameworkPlugin.PLUGIN_ID, IStatus.OK,msg.getString("ShowResultView.OK"),null); //$NON-NLS-1$
		}
	}
	
}