package ch.hsr.ifs.cute.elevator.test.checker;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.cute.elevator.checker.InitializationChecker;

public class DefaultConstructorPositiveMatchesTest extends CDTTestingCodanCheckerTest {

	private Integer expectedMarkerLines[];

	@Override
	protected String getProblemId() {
		return InitializationChecker.DEFAULT_CTOR;
	}
	
	@Before
	public void setUp() throws Exception {
	    addIncludeDirPath("include");
	    super.setUp();
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		assertProblemMarkerPositions(expectedMarkerLines);
	}
	
	@Override
	protected void configureTest(Properties properties) {
		String[] markerLines = properties.getProperty("expectedMarkerLines", "1").split(",");
		expectedMarkerLines = new Integer[markerLines.length];
		for (int i = 0; i < markerLines.length; i++) {
			expectedMarkerLines[i] = Integer.parseInt(markerLines[i]);
		}
	}
}