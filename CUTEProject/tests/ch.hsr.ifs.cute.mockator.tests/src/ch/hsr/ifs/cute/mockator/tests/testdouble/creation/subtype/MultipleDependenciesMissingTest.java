package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class MultipleDependenciesMissingTest extends CDTTestingCheckerTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
    }

    @Test
    public void testTestDoubleAlreadyProvided() throws Throwable {
        assertMarkerLines(17, 18);
        assertMarkerMessages(new String[] { "Object seam \"dep1\" cannot be resolved", "Object seam \"dep2\" cannot be resolved" });
    }
}
