package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public abstract class CodanArguments {

   private static final String MISMATCH_CODAN_ARGUMENTS_MSG = "Wrong number of problem arguments passed; expected: %d, got: %d";

   public String[] getProblemArguments(final IMarker marker) {
      Assert.notNull(marker, "Marker should not be null");
      final String[] problemArguments = CodanProblemMarker.getProblemArguments(marker);
      final int actualNumArgs = problemArguments.length;
      final int expectedNumArgs = getNumOfProblemArguments();
      final String mismatchText = getMismatchText(actualNumArgs, expectedNumArgs);
      Assert.isTrue(actualNumArgs == expectedNumArgs, mismatchText);
      return problemArguments;
   }

   public abstract Object[] toArray();

   public abstract int getNumOfProblemArguments();

   private static String getMismatchText(final int actualNumArgs, final int expectedNumArgs) {
      return String.format(MISMATCH_CODAN_ARGUMENTS_MSG, expectedNumArgs, actualNumArgs);
   }
}
