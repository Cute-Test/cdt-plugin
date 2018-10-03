package ch.hsr.ifs.cute.mockator.tests.incompleteclass.staticpoly;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class WithStlVectorNotMissingTest extends CDTTestingCheckerTest {

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
    }

    @Test
    public void runTest() throws Throwable {
        assertTrue(findMarkers().length == 0);
    }
}
