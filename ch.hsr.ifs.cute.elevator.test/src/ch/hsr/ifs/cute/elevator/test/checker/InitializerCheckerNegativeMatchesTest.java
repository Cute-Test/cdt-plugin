package ch.hsr.ifs.cute.elevator.test.checker;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.cute.elevator.checker.InitializerChecker;

public class InitializerCheckerNegativeMatchesTest extends CDTTestingCodanCheckerTest {

	@Override
	protected String getProblemId() {
		return InitializerChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		assertTrue(findMarkers().length == 0);
	}
}