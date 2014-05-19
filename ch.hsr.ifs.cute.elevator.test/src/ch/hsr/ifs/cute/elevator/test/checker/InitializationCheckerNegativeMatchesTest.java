package ch.hsr.ifs.cute.elevator.test.checker;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.cute.elevator.checker.InitializationChecker;

public class InitializationCheckerNegativeMatchesTest extends CDTTestingCodanCheckerTest {

	@Override
	protected String getProblemId() {
		return InitializationChecker.UNINITIALIZED_VAR;
	}

    @Before
    public void setUp() throws Exception {
        addIncludeDirPath("include");
        super.setUp();
    }

	@Override
	@Test
	public void runTest() throws Throwable {
		assertTrue(findMarkers().length == 0);
	}
}