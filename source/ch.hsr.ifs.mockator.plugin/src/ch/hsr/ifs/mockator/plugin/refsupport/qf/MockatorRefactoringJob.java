package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.hsr.ifs.mockator.plugin.MockatorPlugin;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;

class MockatorRefactoringJob extends Job {
  private final MockatorRefactoring refactoring;
  private final F1V<ChangeEdit> uiCallBack;

  public MockatorRefactoringJob(MockatorRefactoring refactoring, F1V<ChangeEdit> uiCallBack) {
    super(refactoring.getDescription());
    this.refactoring = refactoring;
    this.uiCallBack = uiCallBack;
    // let's schedule this job as soon as possible but we do not want to
    // slow down any interactive jobs; short is a good trade-off value
    setPriority(Job.SHORT);
  }

  @Override
  protected IStatus run(IProgressMonitor pm) {
    try {
      ChangeEdit changeEdit = new MockatorRefactoringExecutor().apply(refactoring, pm);

      if (pm.isCanceled())
        return Status.CANCEL_STATUS;

      UiUtil.runInDisplayThread(uiCallBack, changeEdit);
      return Status.OK_STATUS;
    } catch (MockatorException e) {
      return new Status(IStatus.ERROR, MockatorPlugin.PLUGIN_ID, e.getMessage());
    }
  }
}
