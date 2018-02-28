package ch.hsr.ifs.mockator.plugin.tests.preprocessor;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class TraceFunctionCheckerTest extends CDTTestingCodanCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.TRACE_FUNCTIONS;
   }

   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 2;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Manage trace function \"mockator_srand\"" });
   }
}
