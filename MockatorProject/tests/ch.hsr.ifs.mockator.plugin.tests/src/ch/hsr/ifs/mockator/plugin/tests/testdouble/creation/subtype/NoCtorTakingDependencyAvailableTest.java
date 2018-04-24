package ch.hsr.ifs.mockator.plugin.tests.testdouble.creation.subtype;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.testing.highlevel.cdttest.CDTTestingCheckerTest;

import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;


public class NoCtorTakingDependencyAvailableTest extends CDTTestingCheckerTest {

   @Override
   protected IProblemId getProblemId() {
      return ProblemId.MISSING_TEST_DOUBLE_SUBTYPE;
   }

   @Test
   public void testTestDoubleAlreadyProvided() throws Throwable {
      assertTrue(findMarkers().length == 0);
   }
}
