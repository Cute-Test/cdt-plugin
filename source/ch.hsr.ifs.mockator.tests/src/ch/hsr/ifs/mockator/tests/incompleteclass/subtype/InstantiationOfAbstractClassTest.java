package ch.hsr.ifs.mockator.tests.incompleteclass.subtype;

import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanCheckerTest;
import ch.hsr.ifs.mockator.plugin.incompleteclass.subtype.SubtypePolymorphismChecker;

public class InstantiationOfAbstractClassTest extends CDTTestingCodanCheckerTest {

  @Override
  protected String getProblemId() {
    return SubtypePolymorphismChecker.SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
  }

  @Override
  @Test
  public void runTest() throws Throwable {
    int markerExpectedOnLine = 4;
    assertProblemMarkerPositions(markerExpectedOnLine);
    assertProblemMarkerMessages(new String[] {"Necessary member function(s) not existing in class Foo"});
  }
}
