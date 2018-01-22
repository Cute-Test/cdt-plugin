package ch.hsr.ifs.mockator.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class CtorDependencyToClassWithBaseVirtualDtorShouldBeMarkedTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE.getId();
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      final int markerExpectedOnLine = 15;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Object seam \"dependency\" cannot be resolved" });
   }
}
