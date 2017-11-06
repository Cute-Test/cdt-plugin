package ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.MissingTestDoubleStaticPolyChecker;


public class TestDoubleMissingTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return MissingTestDoubleStaticPolyChecker.MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      final int markerExpectedOnLine = 6;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Compile seam \"Fake\" cannot be resolved" });
   }
}
