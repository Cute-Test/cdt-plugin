package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class PassedToFreeFunctionTest extends CDTTestingCheckerTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
    }

    @Test
    public void testTestDoubleAlreadyProvided() throws Throwable {
        final int markerExpectedOnLine = 9;
        assertMarkerLines(markerExpectedOnLine);
        assertMarkerMessages(new String[] { "Object seam \"dependency\" cannot be resolved" });
    }
}
