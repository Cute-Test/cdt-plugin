package ch.hsr.ifs.cute.constificator.test.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;

public abstract class CheckerTest extends CDTTestingCodanCheckerTest {

	protected List<Integer> markerList;

	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("include/checker");
		super.setUp();
	}
	
	@Override
	protected void configureTest(Properties properties) {
		String markers = properties.getProperty("markerLines");

		if (markers == null) {
			markerList = null;
		} else {
			String[] splitMarkers = markers.split(",");
			markerList = new ArrayList<Integer>(splitMarkers.length);
			for (String marker : splitMarkers) {
				markerList.add(Integer.valueOf(marker));
			}
		}
	}

	@Test
	@Override
	public void runTest() throws Throwable {
		if (markerList == null) {
			assertProblemMarkerPositions();
		} else {
			assertProblemMarkerPositions(markerList.toArray(new Integer[markerList.size()]));
		}
	}
}
