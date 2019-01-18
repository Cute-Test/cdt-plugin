/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.PartInitException
import org.eclipse.ui.progress.UIJob
import ch.hsr.ifs.testframework.Messages
import ch.hsr.ifs.testframework.TestFrameworkPlugin

class ShowResultView : UIJob(msg.getString("ShowResultView.ShowResultView")) {

	companion object {
		private val msg = TestFrameworkPlugin.messages
	}

	override fun runInUIThread(monitor: IProgressMonitor?) =
		TestFrameworkPlugin.activePage?.run {
			try {
				showView(TestRunnerViewPart.ID)
				Status(
					IStatus.OK,
					TestFrameworkPlugin.PLUGIN_ID,
					IStatus.OK,
					msg.getString("ShowResultView.OK"),
					null
				)
			} catch (e: PartInitException) {
				Status(
					IStatus.ERROR,
					TestFrameworkPlugin.PLUGIN_ID,
					msg.getString("ShowResultView.CouldNotShowResultView"),
					e
				)
			}
		}

}