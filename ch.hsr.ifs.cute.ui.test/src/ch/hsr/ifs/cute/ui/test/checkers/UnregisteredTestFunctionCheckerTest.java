package ch.hsr.ifs.cute.ui.test.checkers;

import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;

public class UnregisteredTestFunctionCheckerTest extends CDTTestingCodanCheckerTest {

	private boolean noMarker;
	private int markerLineNr;

	@Override
	@Test
	public void runTest() throws Exception {
		if (!noMarker) {
			assertProblemMarker("Test function is not registered.", markerLineNr);
		} else {
			assertTrue(findMarkers().length == 0);
		}
	}

	@Override
	protected String getProblemId() {
		return "ch.hsr.ifs.cute.unregisteredTestMarker";
	}

	@Override
	protected void configureTest(Properties properties) {
		noMarker = Boolean.parseBoolean(properties.getProperty("noMarker", "false"));
		markerLineNr = Integer.parseInt(properties.getProperty("markerLineNr", "-1"));
	}
}
