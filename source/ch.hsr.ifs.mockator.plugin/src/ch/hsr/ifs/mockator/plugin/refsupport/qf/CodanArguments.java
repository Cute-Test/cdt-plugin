package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


@SuppressWarnings("restriction")
public abstract class CodanArguments {

   private static final String MISMATCH_CODAN_ARGUMENTS_MSG = "Wrong number of problem arguments passed; expected: %d, got: %d";

   public String[] getProblemArguments(IMarker marker) {
      Assert.notNull(marker, "Marker should not be null");
      String[] problemArguments = CodanProblemMarker.getProblemArguments(marker);
      int actualNumArgs = problemArguments.length;
      int expectedNumArgs = getNumOfProblemArguments();
      String mismatchText = getMismatchText(actualNumArgs, expectedNumArgs);
      Assert.isTrue(actualNumArgs == expectedNumArgs, mismatchText);
      return problemArguments;
   }

   public abstract Object[] toArray();

   public abstract int getNumOfProblemArguments();

   private static String getMismatchText(int actualNumArgs, int expectedNumArgs) {
      return String.format(MISMATCH_CODAN_ARGUMENTS_MSG, expectedNumArgs, actualNumArgs);
   }
}
