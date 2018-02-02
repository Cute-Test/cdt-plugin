package ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class MultipleFakeObjectsTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL.getId();
   }

   @Test
   public void runTest() throws Throwable {
      assertProblemMarkerPositions(13, 16);
      assertProblemMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake1",
                                                 "Necessary member function(s) not existing in class Fake2" });
   }
}
