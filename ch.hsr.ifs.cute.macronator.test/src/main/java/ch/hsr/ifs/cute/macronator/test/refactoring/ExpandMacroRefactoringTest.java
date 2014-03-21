package ch.hsr.ifs.cute.macronator.test.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingRefactoringTest;
import ch.hsr.ifs.cute.macronator.refactoring.ExpandMacroRefactoring;

public class ExpandMacroRefactoringTest extends CDTTestingRefactoringTest {

	@Override
	protected Refactoring createRefactoring() {
		return new ExpandMacroRefactoring(getActiveCElement(), selection, cproject);
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		openActiveFileInEditor();
		runRefactoringAndAssertSuccess();
	}
}
