package ch.hsr.ifs.cute.mockator.tests.fakeobject;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.fakeobject.FakeObjectRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class FakeObjectRefactoringTest extends AbstractRefactoringTest {

    @Override
    protected Refactoring createRefactoring() {
        return new FakeObjectRefactoring(CppStandard.Cpp03Std, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
                getCurrentCProject());
    }
}
