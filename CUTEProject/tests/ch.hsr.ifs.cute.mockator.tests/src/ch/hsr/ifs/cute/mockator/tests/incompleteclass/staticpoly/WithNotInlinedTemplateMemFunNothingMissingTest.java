package ch.hsr.ifs.cute.mockator.tests.incompleteclass.staticpoly;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.cute.mockator.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class WithNotInlinedTemplateMemFunNothingMissingTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
