package ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;

import ch.hsr.ifs.iltis.testing.core.cdttest.CDTTestingCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class MemFunPtrDependencyInjectionShouldBeMarkedTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      final int markerExpectedOnLine = 13;
      assertMarkerLines(markerExpectedOnLine);
      assertMarkerMessages(new String[] { "Object seam \"foo\" cannot be resolved" });
   }
}
