package ch.hsr.ifs.cute.ui.test.checkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;

public class UnregisteredTestFunctionCheckerTest extends CDTTestingCodanCheckerTest {

	private boolean noMarker;
	protected List<Integer> markerPositions;

	@Override
	@Test
	public void runTest() throws Exception {
		if (!noMarker && markerPositions != null) {
			assertProblemMarkerPositions(markerPositions.toArray(new Integer[markerPositions.size()]));
		} else {
			assertProblemMarkerPositions();
		}
	}

	@Override
	protected String getProblemId() {
		return "ch.hsr.ifs.cute.unregisteredTestMarker";
	}

	@Override
	protected void configureTest(Properties properties) {
		String markerPositionsString = properties.getProperty("markerLineNr");
		if(markerPositionsString == null)  {
			markerPositions = null;
		}
		else  {
			String[] markerPositionsArray = markerPositionsString.split(",");
			markerPositions = new ArrayList<Integer>();
			for(String markerPosition : markerPositionsArray)  {
				markerPositions.add(Integer.valueOf(markerPosition));
			}
		}
		noMarker = Boolean.parseBoolean(properties.getProperty("noMarker", "false"));
	}
	
}
