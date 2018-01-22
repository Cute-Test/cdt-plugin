package ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class TestDoubleMissingTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_STATICPOLY.getId();
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      final int markerExpectedOnLine = 6;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Compile seam \"Fake\" cannot be resolved" });
   }
}
