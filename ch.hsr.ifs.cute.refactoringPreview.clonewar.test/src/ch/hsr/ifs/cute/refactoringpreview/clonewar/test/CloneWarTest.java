package ch.hsr.ifs.cute.refactoringpreview.clonewar.test;

import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingRefactoringTest;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.CloneWarRefactoring;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.TransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;

public class CloneWarTest extends CDTTestingRefactoringTest {
	private final String EXPECT_PROBLEM_KEY = "expectProblem";

	private final String NESTED_ONLY_KEY = "nestedOnly";
	private final String EXPECTED_FINAL_ERRORS = "expectedFinalErrors";
	protected CloneWarRefactoring refactoring;
	private boolean expectProblem;

	private boolean nestedOnly;

	@Override
	protected Refactoring createRefactoring() {
		refactoring = new CloneWarRefactoring(selection, getActiveCElement(), cproject);
		return refactoring;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		openActiveFileInEditor();
		if (expectProblem) {
			runRefactoringAndAssertFailure();
		} else {
			runRefactoringAndAssertSuccess();
		}
	}

	@Override
	protected void configureTest(Properties properties) {
		expectProblem = Boolean.parseBoolean(properties.getProperty(EXPECT_PROBLEM_KEY, "false"));
		nestedOnly = Boolean.parseBoolean(properties.getProperty(NESTED_ONLY_KEY, "false"));
		expectedFinalErrors = Integer.parseInt(properties.getProperty(EXPECTED_FINAL_ERRORS, "0"));
		super.configureTest(properties);
	}

	@Override
	protected void simulateUserInput(RefactoringContext context) {
		TransformConfiguration config = refactoring.getTransformation().getConfig();
		if (nestedOnly) {
			for (TransformAction action : config.getAllActions()) {
				if (!(action.getNode().getParent() instanceof ICPPASTTypeId))
					action.setPerform(false);
			}
		}
		super.simulateUserInput(context);
	}

}
