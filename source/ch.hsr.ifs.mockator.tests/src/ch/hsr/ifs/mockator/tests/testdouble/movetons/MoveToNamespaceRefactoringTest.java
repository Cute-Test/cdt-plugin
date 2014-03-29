package ch.hsr.ifs.mockator.tests.testdouble.movetons;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.MoveTestDoubleToNsRefactoring;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;

public class MoveToNamespaceRefactoringTest extends MockatorRefactoringTest {

  @Override
  protected MockatorRefactoring createRefactoring() {
    return new MoveTestDoubleToNsRefactoring(CppStandard.Cpp11Std, getActiveCElement(), selection,
        cproject);
  }
}
