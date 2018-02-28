package ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class MultipleDependenciesMissingTest extends CDTTestingCodanCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      assertProblemMarkerPositions(17, 18);
      assertProblemMarkerMessages(new String[] { "Object seam \"dep1\" cannot be resolved", "Object seam \"dep2\" cannot be resolved" });
   }
}
