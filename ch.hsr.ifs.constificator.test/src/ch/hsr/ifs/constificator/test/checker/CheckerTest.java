package ch.hsr.ifs.constificator.test.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;

public abstract class CheckerTest extends CDTTestingCodanCheckerTest {

	protected List<Integer> m_markers;

	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("include/checker");
		super.setUp();
	}
	
	@Override
	protected void configureTest(Properties properties) {
		String markers = properties.getProperty("markerLines");

		if (markers == null) {
			m_markers = null;
		} else {
			String[] splitMarkers = markers.split(",");
			m_markers = new ArrayList<Integer>(splitMarkers.length);
			for (String marker : splitMarkers) {
				m_markers.add(Integer.valueOf(marker));
			}
		}
	}

	@Test
	@Override
	public void runTest() throws Throwable {
		if (m_markers == null) {
			assertProblemMarkerPositions();
		} else {
			assertProblemMarkerPositions(m_markers.toArray(new Integer[m_markers.size()]));
		}
	}
}
