package ch.hsr.ifs.cute.ui.tests.checkers;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;

import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class UnregisteredTestFunctionCheckerTest extends CDTTestingCheckerTest {

   @Test
   public void runTest() throws Exception {
      assertMarkerLines(expectedMarkerLinesFromProperties);
   }

   @Override
   protected IProblemId getProblemId() {
      return IProblemId.wrap("ch.hsr.ifs.cute.unregisteredTestMarker");
   }

}
