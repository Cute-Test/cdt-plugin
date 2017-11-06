package ch.hsr.ifs.mockator.tests.preprocessor;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.preprocessor.qf.TraceFunctionChecker;


public class TraceFunctionCheckerTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return TraceFunctionChecker.TRACE_FUNCTIONS_PROBLEM_ID;
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      final int markerExpectedOnLine = 2;
      assertProblemMarkerPositions(markerExpectedOnLine);
      assertProblemMarkerMessages(new String[] { "Manage trace function \"mockator_srand\"" });
   }
}
