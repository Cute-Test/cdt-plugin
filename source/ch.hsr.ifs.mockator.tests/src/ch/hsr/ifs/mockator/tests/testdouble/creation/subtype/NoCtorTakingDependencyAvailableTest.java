package ch.hsr.ifs.mockator.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.MissingTestDoubleSubTypeChecker;


public class NoCtorTakingDependencyAvailableTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return MissingTestDoubleSubTypeChecker.MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
