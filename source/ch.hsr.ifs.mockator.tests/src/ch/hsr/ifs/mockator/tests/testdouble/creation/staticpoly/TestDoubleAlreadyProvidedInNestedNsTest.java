package ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.MissingTestDoubleStaticPolyChecker;


public class TestDoubleAlreadyProvidedInNestedNsTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return MissingTestDoubleStaticPolyChecker.MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
