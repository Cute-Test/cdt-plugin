package ch.hsr.ifs.cute.macronator.test.quickfix;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cute.macronator.checker.ObjectLikeMacroChecker;
import ch.hsr.ifs.cute.macronator.quickfix.ObjectLikeQuickFix;

public class ObjectLikeQuickfixTest extends CDTTestingCodanQuickfixTest {

	@Override
	protected String getProblemId() {
		return ObjectLikeMacroChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		runQuickFix(new ObjectLikeQuickFix());
		assertEquals(getExpectedSource(), getCurrentSource());
	}
}
