package ch.hsr.ifs.cute.mockator.tests.testdouble.movetons;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.testdouble.movetons.MoveTestDoubleToNsRefactoring;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class MoveToNamespaceRefactoringTest extends AbstractRefactoringTest {

    @Override
    protected MockatorRefactoring createRefactoring() {
        return new MoveTestDoubleToNsRefactoring(CppStandard.Cpp11Std, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
                getCurrentCProject());
    }
}
