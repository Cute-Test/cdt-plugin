//package ch.hsr.ifs.cute.mockator.REMOVE_AFTER_TESTING;
//
//import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.array;
//import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.eclipse.core.resources.IMarker;
//
//import ch.hsr.ifs.iltis.core.core.resources.StringUtil;
//
//import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
//import ch.hsr.ifs.cute.mockator.testdouble.support.MemFunSignature;
//
//
//public class ConsistentExpectationsCodanArgs extends CodanArguments {
//
//   private static final String      HTML_NEW_LINE       = "<br/>";
//   private static final String      SIGNATURE_DELIMITER = "\"";
//   private final Collection<String> expectationsToRemove;
//   private final Collection<String> expectationsToAdd;
//   private final String             resolutionDesc;
//
//   public ConsistentExpectationsCodanArgs(final IMarker marker) {
//      final String[] problemArgs = getProblemArguments(marker);
//      expectationsToRemove = split(problemArgs[0]);
//      expectationsToAdd = split(problemArgs[1]);
//      resolutionDesc = problemArgs[2];
//   }
//
//   public ConsistentExpectationsCodanArgs(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
//      expectationsToRemove = toStrings(toRemove);
//      expectationsToAdd = toStrings(toAdd);
//      resolutionDesc = getResolutionDesc(toRemove, toAdd);
//   }
//
//   private static Collection<String> toStrings(final Collection<MemFunSignature> signatures) {
//      return signatures.stream().map(MemFunSignature::toString).collect(Collectors.toList());
//   }
//
//   private static String getResolutionDesc(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
//      final StringBuilder resolutionDesc = new StringBuilder();
//
//      for (final MemFunSignature sig : toAdd) {
//         resolutionDesc.append("+ " + htmlize(sig) + HTML_NEW_LINE);
//      }
//
//      for (final MemFunSignature sig : toRemove) {
//         resolutionDesc.append("- " + htmlize(sig) + HTML_NEW_LINE);
//      }
//
//      return resolutionDesc.toString();
//   }
//
//   private static String htmlize(final MemFunSignature funCallExpectation) {
//      return StringUtil.quote(StringUtil.CodeString.escapeHtml(StringUtil.unquote(funCallExpectation.toString())));
//   }
//
//   private static List<String> split(final String expectations) {
//      if (expectations.trim().isEmpty()) {
//         return new ArrayList<>();
//      }
//      return list(expectations.split(SIGNATURE_DELIMITER));
//   }
//
//   @Override
//   public Object[] toArray() {
//      return array(toString(expectationsToRemove), toString(expectationsToAdd), resolutionDesc);
//   }
//
//   private static String toString(final Collection<String> expectations) {
//      return expectations.stream().collect(Collectors.joining(SIGNATURE_DELIMITER));
//   }
//
//   public Collection<ExistingMemFunCallRegistration> getExpectationsToRemove() {
//      final Collection<ExistingMemFunCallRegistration> expectations = new LinkedHashSet<>();
//      for (final String expectation : expectationsToRemove) {
//         expectations.add(new ExistingMemFunCallRegistration(expectation));
//      }
//      return expectations;
//   }
//
//   public Collection<String> getExpectationsToAdd() {
//      return expectationsToAdd;
//   }
//
//   public String getResolutionDesc() {
//      return resolutionDesc;
//   }
//
//   @Override
//   public int getNumOfProblemArguments() {
//      return 3;
//   }
//}
