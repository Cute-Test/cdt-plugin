package ch.hsr.ifs.mockator.plugin.tests.incompleteclass.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.cdttest.CDTTestingCheckerTest;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class WithTestDoubleInNsTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 11;
      assertMarkerLines(markerExpectedOnLine);
      assertMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake" });
   }
}
