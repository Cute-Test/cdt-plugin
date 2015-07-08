package ch.hsr.ifs.cute.macronator.test.quickfix;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cute.macronator.checker.UnusedMacroChecker;
import ch.hsr.ifs.cute.macronator.quickfix.UnusedMacroQuickfix;
import ch.hsr.ifs.cute.macronator.test.testutils.UIHelper;

public class UnusedMacroQuickfixTest extends CDTTestingCodanQuickfixTest {

	@Override
	protected String getProblemId() {
		return UnusedMacroChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		UIHelper.closeWelcomeScreen();
		openActiveFileInEditor();
		runQuickFix(new UnusedMacroQuickfix());
		assertEquals(getExpectedSource(), getCurrentSource());
	}
}
