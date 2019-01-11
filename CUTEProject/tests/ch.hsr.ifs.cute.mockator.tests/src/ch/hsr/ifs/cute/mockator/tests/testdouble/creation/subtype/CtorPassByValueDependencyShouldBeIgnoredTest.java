package ch.hsr.ifs.cute.mockator.tests.testdouble.creation.subtype;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class CtorPassByValueDependencyShouldBeIgnoredTest extends CDTTestingCheckerTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
    }

    @Test
    public void testTestDoubleAlreadyProvided() throws Throwable {
        assertTrue(findMarkers().length == 0);
    }
}
