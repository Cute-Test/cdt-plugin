package ch.hsr.ifs.mockator.plugin.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.cdttest.CDTTestingCheckerTest;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class InstantiationOfAbstractClassTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.SUBTYPE_MISSING_MEMFUNS_IMPL;
   }

   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 4;
      assertMarkerLines(markerExpectedOnLine);
      assertMarkerMessages(new String[] { "Necessary member function(s) not existing in class Foo" });
   }
}
