package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class TestDoubleMissingTest extends CDTTestingCheckerTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.MISSING_TEST_DOUBLE_STATICPOLY;
    }

    @Test
    public void testTestDoubleAlreadyProvided() throws Throwable {
        final int markerExpectedOnLine = 6;
        assertMarkerLines(markerExpectedOnLine);
        assertMarkerMessages(new String[] { "Compile seam \"Fake\" cannot be resolved" });
    }
}
