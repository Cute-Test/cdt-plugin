package ch.hsr.ifs.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class NonLocalCompleteClassTest extends CDTTestingCodanCheckerTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
