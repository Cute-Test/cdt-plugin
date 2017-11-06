package ch.hsr.ifs.mockator.tests.fakeobject;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;


public class FakeObjectRefactoringTest extends MockatorRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new FakeObjectRefactoring(CppStandard.Cpp03Std, getActiveCElement(), selection, cproject);
   }
}
