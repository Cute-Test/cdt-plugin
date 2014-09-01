package ch.hsr.ifs.cute.charwars.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;

public abstract class BaseQuickFixTest extends CDTTestingCodanQuickfixTest {
	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("commonIncludes");
		super.setUp();
	}
	
	private IMarker getFirstMarker() throws CoreException  {
		IMarker[] markers = findMarkers();
		IMarker firstMarker = markers[0];
		for(IMarker currentMarker : markers)  {
			int startOfFirstMarker = firstMarker.getAttribute(IMarker.CHAR_START, Integer.MAX_VALUE);
			int startOfCurrentMarker = currentMarker.getAttribute(IMarker.CHAR_START, Integer.MAX_VALUE);
			if(startOfFirstMarker > startOfCurrentMarker)  {
				firstMarker = currentMarker;
			}
		}
		return firstMarker;
	}
	
	private String getNormalizedExpectedSource() {
		return getExpectedSource().replace("\n", "").replace("\r", "");
	}
	
	private String getNormalizedCurrentSource() {
		return getCurrentSource().replace("\n", "").replace("\r", "");
	}
	
	@Override
	@Test
	public void runTest() throws Throwable {
		IMarker firstMarker = getFirstMarker();
		runQuickFix(firstMarker, getQuickFix());
		assertEquals(getNormalizedExpectedSource(), getNormalizedCurrentSource());
	}
	
	protected abstract IMarkerResolution getQuickFix();
}