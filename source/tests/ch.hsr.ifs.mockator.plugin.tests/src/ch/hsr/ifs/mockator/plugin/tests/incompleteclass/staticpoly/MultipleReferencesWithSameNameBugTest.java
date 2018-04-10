package ch.hsr.ifs.mockator.plugin.tests.incompleteclass.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class MultipleReferencesWithSameNameBugTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      assertMarkerLines(10, 18);
      assertMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake",
                                          "Necessary member function(s) not existing in class Fake" });
   }
}
