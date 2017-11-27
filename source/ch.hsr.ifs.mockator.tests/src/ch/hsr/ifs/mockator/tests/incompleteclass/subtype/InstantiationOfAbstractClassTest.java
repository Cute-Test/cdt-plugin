package ch.hsr.ifs.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class InstantiationOfAbstractClassTest extends CDTTestingCodanCheckerTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 4;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Necessary member function(s) not existing in class Foo" });
   }
}
