package ch.hsr.ifs.mockator.plugin.tests.fakeobject;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.tests.AbstractRefactoringTest;


public class FakeObjectRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new FakeObjectRefactoring(CppStandard.Cpp03Std, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            getCurrentCProject());
   }
}
