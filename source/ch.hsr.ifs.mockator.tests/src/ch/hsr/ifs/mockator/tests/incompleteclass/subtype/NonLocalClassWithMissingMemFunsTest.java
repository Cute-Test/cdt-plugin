package ch.hsr.ifs.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class NonLocalClassWithMissingMemFunsTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL.getId();
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 7;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake" });
   }
}
