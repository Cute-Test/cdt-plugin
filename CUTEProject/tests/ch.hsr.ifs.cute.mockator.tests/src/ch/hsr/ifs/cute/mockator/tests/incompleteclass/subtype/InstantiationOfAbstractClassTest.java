package ch.hsr.ifs.cute.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class InstantiationOfAbstractClassTest extends CDTTestingCheckerTest {

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 4;
      assertMarkerLines(markerExpectedOnLine);
      assertMarkerMessages(new String[] { "Necessary member function(s) not existing in class Foo" });
   }
}
