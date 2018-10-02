package ch.hsr.ifs.cute.mockator.tests.incompleteclass.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class WithStlContainerImplMissing1Test extends CDTTestingCheckerTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      assertMarkerLines(35);
      assertMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake" });
   }
}
