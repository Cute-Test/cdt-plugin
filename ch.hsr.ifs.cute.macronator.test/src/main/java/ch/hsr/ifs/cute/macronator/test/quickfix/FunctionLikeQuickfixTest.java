package ch.hsr.ifs.cute.macronator.test.quickfix;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cute.macronator.checker.FunctionLikeMacroChecker;
import ch.hsr.ifs.cute.macronator.quickfix.FunctionLikeQuickFix;
import ch.hsr.ifs.cute.macronator.test.testutils.UIHelper;

public class FunctionLikeQuickfixTest extends CDTTestingCodanQuickfixTest {

	@Override
	protected String getProblemId() {
		return FunctionLikeMacroChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		UIHelper.closeWelcomeScreen();
		openActiveFileInEditor();
		runQuickFix(new FunctionLikeQuickFix());
		assertEquals(getExpectedSource(), getCurrentSource());
	}
}
