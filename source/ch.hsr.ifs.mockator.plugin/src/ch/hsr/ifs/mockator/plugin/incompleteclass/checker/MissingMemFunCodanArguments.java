package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;

import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;

public class MissingMemFunCodanArguments extends CodanArguments {
  private final String testDoubleName;
  private final String missingMemFunsForFake;
  private final String missingMemFunsForMock;

  public MissingMemFunCodanArguments(IMarker marker) {
    String[] problemArguments = getProblemArguments(marker);
    testDoubleName = problemArguments[0];
    missingMemFunsForFake = problemArguments[1];
    missingMemFunsForMock = problemArguments[2];
  }

  public MissingMemFunCodanArguments(String testDoubleName, String forFake, String forMock) {
    this.testDoubleName = testDoubleName;
    this.missingMemFunsForFake = forFake;
    this.missingMemFunsForMock = forMock;
  }

  @Override
  public Object[] toArray() {
    return array(testDoubleName, missingMemFunsForFake, missingMemFunsForMock);
  }

  public String getTestDoubleName() {
    return testDoubleName;
  }

  public String getMissingMemFunsForFake() {
    return missingMemFunsForFake;
  }

  public String getMissingMemFunsForMock() {
    return missingMemFunsForMock;
  }

  @Override
  public int getNumOfProblemArguments() {
    return 3;
  }
}
