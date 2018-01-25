package ch.hsr.ifs.mockator.tests.linker.shadowfun;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.linker.shadowfun.ShadowFunctionRefactoring;
import ch.hsr.ifs.mockator.tests.AbstractRefactoringTest;


public class ShadowFunctionRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new ShadowFunctionRefactoring(getActiveCElement(), selection, cproject);
   }
}
