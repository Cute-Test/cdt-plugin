//package ch.hsr.ifs.cute.mockator.REMOVE_AFTER_TESTING;
//
//import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
//import org.eclipse.core.resources.IMarker;
//
//import ch.hsr.ifs.iltis.core.exception.ILTISException;
//
//
//@SuppressWarnings("restriction")
//public abstract class CodanArguments {
//
//   private static final String MISMATCH_CODAN_ARGUMENTS_MSG = "Wrong number of problem arguments passed; expected: %d, got: %d";
//
//   public String[] getProblemArguments(final IMarker marker) {
//      ILTISException.Unless.notNull("Marker should not be null", marker);
//      final String[] problemArguments = CodanProblemMarker.getProblemArguments(marker);
//      final int actualNumArgs = problemArguments.length;
//      final int expectedNumArgs = getNumOfProblemArguments();
//      ILTISException.Unless.isTrue(getMismatchText(actualNumArgs, expectedNumArgs), actualNumArgs == expectedNumArgs);
//      return problemArguments;
//   }
//
//   public abstract Object[] toArray();
//
//   public abstract int getNumOfProblemArguments();
//
//   private static String getMismatchText(final int actualNumArgs, final int expectedNumArgs) {
//      return String.format(MISMATCH_CODAN_ARGUMENTS_MSG, expectedNumArgs, actualNumArgs);
//   }
//}
