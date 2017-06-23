package ch.hsr.ifs.cute.charwars.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cdttesting.testsourcefile.TestSourceFile;

public abstract class BaseQuickFixTest extends CDTTestingCodanQuickfixTest {
	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("commonIncludes");
		super.setUp();
	}

	private IMarker getFirstMarker() throws CoreException  {
		final IMarker[] markers = findMarkers();
		IMarker firstMarker = markers[0];
		for(final IMarker currentMarker : markers)  {
			final int startOfFirstMarker = firstMarker.getAttribute(IMarker.CHAR_START, Integer.MAX_VALUE);
			final int startOfCurrentMarker = currentMarker.getAttribute(IMarker.CHAR_START, Integer.MAX_VALUE);
			if(startOfFirstMarker > startOfCurrentMarker)  {
				firstMarker = currentMarker;
			}
		}
		return firstMarker;
	}

	@Override
	@Test
	public void runTest() throws Throwable {
		final IMarker firstMarker = getFirstMarker();
		runQuickFix(firstMarker, getQuickFix());
		compareFiles();
	}

	private void compareFiles() {
		for (final TestSourceFile testFile : fileMap.values()) {
			final String expectedSource = normalizeSource(testFile.getExpectedSource());
			final String actualSource = normalizeSource(getCurrentSource(testFile.getName()));
			assertEquals(expectedSource, actualSource);
		}
	}

	private static String normalizeSource(String source) {
		return source.replace("\n", "").replace("\r", "");
	}

	protected abstract IMarkerResolution getQuickFix();
}