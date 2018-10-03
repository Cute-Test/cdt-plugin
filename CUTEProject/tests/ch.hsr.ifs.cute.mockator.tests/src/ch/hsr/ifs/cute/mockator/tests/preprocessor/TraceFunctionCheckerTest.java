package ch.hsr.ifs.cute.mockator.tests.preprocessor;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class TraceFunctionCheckerTest extends CDTTestingCheckerTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.TRACE_FUNCTIONS;
    }

    @Test
    public void runTest() throws Throwable {
        final int markerExpectedOnLine = 2;
        assertMarkerLines(markerExpectedOnLine);
        assertMarkerMessages(new String[] { "Manage trace function \"mockator_srand\"" });
    }
}
