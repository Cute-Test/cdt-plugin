package ch.hsr.ifs.mockator.plugin.tests.linker.shadowfun;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.linker.shadowfun.ShadowFunctionRefactoring;
import ch.hsr.ifs.mockator.plugin.tests.AbstractRefactoringTest;


public class ShadowFunctionRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new ShadowFunctionRefactoring(getActiveCElement(), selection, currentCproject);
   }
}
