package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;


class MockatorRefactoringJob extends Job {

   private final MockatorRefactoring  refactoring;
   private final Consumer<ChangeEdit> uiCallBack;

   public MockatorRefactoringJob(final MockatorRefactoring refactoring, final Consumer<ChangeEdit> uiCallBack) {
      super(refactoring.getDescription());
      this.refactoring = refactoring;
      this.uiCallBack = uiCallBack;
      // let's schedule this job as soon as possible but we do not want to
      // slow down any interactive jobs; short is a good trade-off value
      setPriority(Job.SHORT);
   }

   @Override
   protected IStatus run(final IProgressMonitor pm) {
      try {
         final ChangeEdit changeEdit = new MockatorRefactoringExecutor().apply(refactoring, pm);

         if (pm.isCanceled()) { return Status.CANCEL_STATUS; }

         UiUtil.runInDisplayThread(uiCallBack, changeEdit);
         return Status.OK_STATUS;
      }
      catch (final RuntimeException e) {
         return new Status(IStatus.ERROR, MockatorPlugin.PLUGIN_ID, e.getMessage());
      }
   }
}
