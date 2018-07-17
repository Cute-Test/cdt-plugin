package ch.hsr.ifs.cute.mockator.tests.linker.shadowfun;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;
import ch.hsr.ifs.cute.mockator.linker.shadowfun.ShadowFunctionRefactoring;


public class ShadowFunctionRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new ShadowFunctionRefactoring(getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(), getCurrentCProject());
   }
}
