package ch.hsr.ifs.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class WithMultipleInheritanceHierarchiesTest extends CDTTestingCodanCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 9;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake" });
   }
}
