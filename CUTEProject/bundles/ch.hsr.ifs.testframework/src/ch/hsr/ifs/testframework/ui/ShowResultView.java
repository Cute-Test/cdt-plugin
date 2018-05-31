/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.testframework.Messages;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;


public class ShowResultView extends UIJob {

   private static Messages msg = TestFrameworkPlugin.getMessages();

   public ShowResultView() {
      super(msg.getString("ShowResultView.ShowResultView"));
   }

   private TestRunnerViewPart showTestRunnerViewPartInActivePage(TestRunnerViewPart testRunner) {
      IWorkbenchPage page = null;
      try {
         page = TestFrameworkPlugin.getActivePage();
         if (page == null) return null;
         if (testRunner != null && testRunner.isCreated()) {
            page.activate(testRunner);
            return testRunner;
         }
         return (TestRunnerViewPart) page.showView(TestRunnerViewPart.ID);
      } catch (PartInitException pie) {
         TestFrameworkPlugin.log(pie);
         return null;
      }
   }

   private TestRunnerViewPart findTestRunnerViewPartInActivePage() {
      IWorkbenchPage page = TestFrameworkPlugin.getActivePage();
      if (page == null) return null;
      return (TestRunnerViewPart) page.findView(TestRunnerViewPart.ID);
   }

   @Override
   public IStatus runInUIThread(IProgressMonitor monitor) {
      if (showTestRunnerViewPartInActivePage(findTestRunnerViewPartInActivePage()) == null) {
         return new Status(IStatus.WARNING, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("ShowResultView.CouldNotShowResultView"), null);
      } else {
         return new Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("ShowResultView.OK"), null);
      }
   }

}
