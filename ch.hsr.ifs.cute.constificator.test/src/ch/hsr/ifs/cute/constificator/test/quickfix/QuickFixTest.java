package ch.hsr.ifs.cute.constificator.test.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;

public abstract class QuickFixTest extends CDTTestingCodanQuickfixTest {

	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("include/checker");
		super.setUp();
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
    	IMarker[] markers = findMarkers();
    	boolean hasMarkers = markers.length > 0;
    	while(hasMarkers) {
    		runQuickFix(markers[0], getQuickFix());
    		markers = findMarkers();
    		hasMarkers = markers.length > 0;
    	}
        assertEquals(getNormalizedExpectedSource(), getNormalizedCurrentSource());
    }

    protected abstract IMarkerResolution getQuickFix();



}
