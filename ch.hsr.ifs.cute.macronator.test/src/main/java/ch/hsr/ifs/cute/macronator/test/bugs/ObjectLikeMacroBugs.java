package ch.hsr.ifs.cute.macronator.test.bugs;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.cute.macronator.checker.ObjectLikeMacroChecker;

public class ObjectLikeMacroBugs extends CDTTestingCodanCheckerTest {

	@Override
	protected String getProblemId() {
		return ObjectLikeMacroChecker.PROBLEM_ID;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		assertEquals(0, findMarkers().length);
	}
}
