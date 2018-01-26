package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.array;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;


public class MissingMemFunCodanArguments extends CodanArguments {

   private final String testDoubleName;
   private final String missingMemFunsForFake;
   private final String missingMemFunsForMock;

   public MissingMemFunCodanArguments(final IMarker marker) {
      final String[] problemArguments = getProblemArguments(marker);
      testDoubleName = problemArguments[0];
      missingMemFunsForFake = problemArguments[1];
      missingMemFunsForMock = problemArguments[2];
   }

   public MissingMemFunCodanArguments(final String testDoubleName, final String forFake, final String forMock) {
      this.testDoubleName = testDoubleName;
      missingMemFunsForFake = forFake;
      missingMemFunsForMock = forMock;
   }

   @Override
   public Object[] toArray() {
      return array(testDoubleName, missingMemFunsForFake, missingMemFunsForMock);
   }

   public List<Object> toList() {
      return Arrays.asList(toArray());
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
