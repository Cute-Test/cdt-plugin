package ch.hsr.ifs.cute.mockator.tests.mockobject.function;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.mockobject.function.MockFunctionRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class MockFunctionRefactoringTest extends AbstractRefactoringTest {

    private CppStandard cppStandard;

    @Override
    protected void configureTest(final Properties refactoringProperties) {
        super.configureTest(refactoringProperties);
        cppStandard = CppStandard.fromName(refactoringProperties.getProperty("cppStandard", "C++03"));
        markerCount = 0;
    }

    @Override
    protected Refactoring createRefactoring() {
        return new MockFunctionRefactoring(cppStandard, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
                getCurrentCProject(), getCurrentCProject());
    }
}
