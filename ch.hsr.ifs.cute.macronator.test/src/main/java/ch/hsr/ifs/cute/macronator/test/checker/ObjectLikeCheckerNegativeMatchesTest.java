package ch.hsr.ifs.cute.macronator.test.checker;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.cute.macronator.checker.ObjectLikeMacroChecker;

public class ObjectLikeCheckerNegativeMatchesTest extends CDTTestingCodanCheckerTest {

	@Override
	protected String getProblemId() {
		return ObjectLikeMacroChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		assertTrue(findMarkers().length == 0);
	}
}