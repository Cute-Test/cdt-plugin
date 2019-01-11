package ch.hsr.ifs.cute.mockator.tests.mockobject.convert;

import java.util.Properties;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.mockobject.convert.ConvertToMockObjectRefactoring;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class ConvertToMockObjectRefactoringTest extends AbstractRefactoringTest {

    private CppStandard            cppStandard;
    private LinkedEditModeStrategy linkedEditStrategy;

    @Override
    protected void configureTest(final Properties refactoringProperties) {
        super.configureTest(refactoringProperties);
        cppStandard = CppStandard.fromName(refactoringProperties.getProperty("cppStandard"));
        linkedEditStrategy = LinkedEditModeStrategy.fromName(refactoringProperties.getProperty("linkedEditStrategy", "ChooseFunctions"));
        markerCount = 0;
        withCuteNature = true;
    }

    @Override
    protected Refactoring createRefactoring() {
        return new ConvertToMockObjectRefactoring(cppStandard, getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(),
                getCurrentCProject(), linkedEditStrategy);
    }
}
