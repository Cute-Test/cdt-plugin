package ch.hsr.ifs.cute.macronator.test.checker;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.cute.macronator.checker.FunctionLikeMacroChecker;

public class FunctionLikeCheckerNegativeMatchesTest extends CDTTestingCodanCheckerTest {

	@Override
	protected String getProblemId() {
		return FunctionLikeMacroChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		assertEquals(0, findMarkers().length);
	}
}