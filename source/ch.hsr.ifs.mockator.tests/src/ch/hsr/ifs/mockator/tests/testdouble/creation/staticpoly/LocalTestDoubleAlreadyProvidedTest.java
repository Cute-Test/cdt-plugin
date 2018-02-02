package ch.hsr.ifs.mockator.tests.testdouble.creation.staticpoly;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class LocalTestDoubleAlreadyProvidedTest extends CDTTestingCodanCheckerTest {

   @Override
   protected String getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_STATICPOLY.getId();
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
