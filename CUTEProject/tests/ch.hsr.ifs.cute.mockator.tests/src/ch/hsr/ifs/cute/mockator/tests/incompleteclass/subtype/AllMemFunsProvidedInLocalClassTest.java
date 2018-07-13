package ch.hsr.ifs.cute.mockator.tests.incompleteclass.subtype;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;

import ch.hsr.ifs.cute.mockator.base.misc.IdHelper.ProblemId;


public class AllMemFunsProvidedInLocalClassTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
