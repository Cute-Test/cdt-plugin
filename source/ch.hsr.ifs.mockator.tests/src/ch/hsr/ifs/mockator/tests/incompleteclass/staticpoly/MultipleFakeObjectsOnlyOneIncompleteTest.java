package ch.hsr.ifs.mockator.tests.incompleteclass.staticpoly;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.StaticPolymorphismChecker;

public class MultipleFakeObjectsOnlyOneIncompleteTest extends CDTTestingCodanCheckerTest {

  @Override
  protected String getProblemId() {
    return StaticPolymorphismChecker.STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
  }

  @Override
  @Test
  public void runTest() throws Throwable {
    int markerExpectedOnLine = 16;
    assertProblemMarkerPositions(markerExpectedOnLine);
    assertProblemMarkerMessages(new String[] { "Necessary member function(s) not existing in class Fake2" });
  }
}
