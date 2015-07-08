package ch.hsr.ifs.cute.charwars.checkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;

public abstract class BaseCheckerTest extends CDTTestingCodanCheckerTest {
	protected List<Integer> markerPositions;
	
	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("commonIncludes");
		super.setUp();
	}
	
	@Override
	protected void configureTest(Properties properties) {
		String markerPositionsString = properties.getProperty("markerPositions");
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
	}
	
	@Override
	@Test
	public void runTest() throws Throwable {
		if(markerPositions != null)  {
			assertProblemMarkerPositions(markerPositions.toArray(new Integer[markerPositions.size()]));
		}
		else  {
			assertProblemMarkerPositions();
		}
	}
}