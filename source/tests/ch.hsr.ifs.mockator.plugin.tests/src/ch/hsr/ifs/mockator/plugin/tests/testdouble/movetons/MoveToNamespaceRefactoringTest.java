package ch.hsr.ifs.mockator.plugin.tests.testdouble.movetons;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.MoveTestDoubleToNsRefactoring;
import ch.hsr.ifs.mockator.plugin.tests.AbstractRefactoringTest;


public class MoveToNamespaceRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected MockatorRefactoring createRefactoring() {
      return new MoveTestDoubleToNsRefactoring(CppStandard.Cpp11Std, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
            getCurrentCProject());
   }
}
