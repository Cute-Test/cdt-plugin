package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;


public class MockatorRefactoringRunner {

   private final MockatorRefactoring refactoring;

   public MockatorRefactoringRunner(MockatorRefactoring refactoring) {
      this.refactoring = refactoring;
   }

   public ChangeEdit runInCurrentThread(IProgressMonitor pm) {
      ChangeEdit changeEdit = new MockatorRefactoringExecutor().apply(refactoring, pm);
      return changeEdit;
   }

   public void runInNewJob(F1V<ChangeEdit> uiCallBack) {
      new MockatorRefactoringJob(refactoring, uiCallBack).schedule();
   }

   public void runInNewJob() {
      new MockatorRefactoringJob(refactoring, new NullFunction()).schedule();
   }

   private static class NullFunction implements F1V<ChangeEdit> {

      @Override
      public void apply(ChangeEdit notUsed) {}
   }
}
