package ch.hsr.ifs.mockator.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.MissingTestDoubleSubTypeChecker;


public class MultipleDependenciesMissingTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return MissingTestDoubleSubTypeChecker.MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      assertProblemMarkerPositions(17, 18);
      assertProblemMarkerMessages(new String[] { "Object seam \"dep1\" cannot be resolved", "Object seam \"dep2\" cannot be resolved" });
   }
}
