package ch.hsr.ifs.cute.mockator.refsupport.qf;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;


public class MockatorRefactoringRunner {

   private final MockatorRefactoring refactoring;

   public MockatorRefactoringRunner(final MockatorRefactoring refactoring) {
      this.refactoring = refactoring;
   }

   public ChangeEdit runInCurrentThread(final IProgressMonitor pm) {
      final ChangeEdit changeEdit = new MockatorRefactoringExecutor().apply(refactoring, pm);
      return changeEdit;
   }

   public void runInNewJob(final Consumer<ChangeEdit> uiCallBack) {
      new MockatorRefactoringJob(refactoring, uiCallBack).schedule();
   }

   public void runInNewJob() {
      new MockatorRefactoringJob(refactoring, new NullFunction()).schedule();
   }

   private static class NullFunction implements Consumer<ChangeEdit> {

      @Override
      public void accept(final ChangeEdit notUsed) {}
   }
}
