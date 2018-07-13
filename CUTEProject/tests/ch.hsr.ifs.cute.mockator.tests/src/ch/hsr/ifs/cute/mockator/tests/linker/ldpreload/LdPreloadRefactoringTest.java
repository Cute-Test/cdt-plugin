package ch.hsr.ifs.cute.mockator.tests.linker.ldpreload;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;
import ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.refactoring.LdPreloadRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


public class LdPreloadRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new LdPreloadRefactoring(CppStandard.Cpp11Std, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            getCurrentCProject(), getCurrentProject());
   }
}
