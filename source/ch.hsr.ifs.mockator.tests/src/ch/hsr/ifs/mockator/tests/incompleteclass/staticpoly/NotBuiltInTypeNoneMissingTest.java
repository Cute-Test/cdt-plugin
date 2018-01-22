package ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class NotBuiltInTypeNoneMissingTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL.getId();
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
