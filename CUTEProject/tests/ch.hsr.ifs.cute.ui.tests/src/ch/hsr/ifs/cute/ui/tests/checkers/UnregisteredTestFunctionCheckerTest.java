package ch.hsr.ifs.cute.ui.tests.checkers;

import java.util.List;

import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;

import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingCheckerTest;


public class UnregisteredTestFunctionCheckerTest extends CDTTestingCheckerTest {

   private boolean         noMarker;
   protected List<Integer> markerPositions;

   @Test
   public void runTest() throws Exception {
      if (!noMarker && markerPositions != null) {
         assertMarkerLines(expectedMarkerLinesFromProperties);
      } else {
         assertMarkerLines();
      }
   }

   @Override
   protected IProblemId getProblemId() {
      return IProblemId.wrap("ch.hsr.ifs.cute.unregisteredTestMarker");
   }

}
