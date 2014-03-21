package ch.hsr.ifs.cute.elevator.test.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingRefactoringTest;
import ch.hsr.ifs.cute.elevator.refactoring.ElevateProjectRefactoring;

public class ElevateProjectRefactoringTest extends CDTTestingRefactoringTest {

    @Override
    protected Refactoring createRefactoring() {
        return new ElevateProjectRefactoring(getActiveCElement(), selection, cproject);
    }
    
    @Override
    @Test
    public void runTest() throws Throwable {
        openActiveFileInEditor();
        runRefactoringAndAssertSuccess();
    }
}
