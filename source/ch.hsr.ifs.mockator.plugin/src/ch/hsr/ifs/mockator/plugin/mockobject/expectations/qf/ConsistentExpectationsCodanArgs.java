package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.util.StringUtil.quote;
import static ch.hsr.ifs.mockator.plugin.base.util.StringUtil.unquote;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.iltis.core.resources.StringUtil;

import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;


class ConsistentExpectationsCodanArgs extends CodanArguments {

   private static final String      HTML_NEW_LINE       = "<br/>";
   private static final String      SIGNATURE_DELIMITER = "\"";
   private final Collection<String> expectationsToRemove;
   private final Collection<String> expectationsToAdd;
   private final String             resolutionDesc;

   public ConsistentExpectationsCodanArgs(final IMarker marker) {
      final String[] problemArgs = getProblemArguments(marker);
      expectationsToRemove = split(problemArgs[0]);
      expectationsToAdd = split(problemArgs[1]);
      resolutionDesc = problemArgs[2];
   }

   public ConsistentExpectationsCodanArgs(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      expectationsToRemove = toStrings(toRemove);
      expectationsToAdd = toStrings(toAdd);
      resolutionDesc = getResolutionDesc(toRemove, toAdd);
   }

   private static Collection<String> toStrings(final Collection<MemFunSignature> signatures) {
      return signatures.stream().map((expectation) -> expectation.toString()).collect(Collectors.toList());
   }

   private static String getResolutionDesc(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      final StringBuilder resolutionDesc = new StringBuilder();

      for (final MemFunSignature sig : toAdd) {
         resolutionDesc.append("+ " + htmlize(sig) + HTML_NEW_LINE);
      }

      for (final MemFunSignature sig : toRemove) {
         resolutionDesc.append("- " + htmlize(sig) + HTML_NEW_LINE);
      }

      return resolutionDesc.toString();
   }

   private static String htmlize(final MemFunSignature funCallExpectation) {
      return quote(StringUtil.CodeString.escapeHtml(unquote(funCallExpectation.toString())));
   }

   private static List<String> split(final String expectations) {
      if (expectations.trim().isEmpty()) {
         return list();
      }
      return list(expectations.split(SIGNATURE_DELIMITER));
   }

   @Override
   public Object[] toArray() {
      return array(toString(expectationsToRemove), toString(expectationsToAdd), resolutionDesc);
   }

   private static String toString(final Collection<String> expectations) {
      return expectations.stream().collect(Collectors.joining(SIGNATURE_DELIMITER));
   }

   public Collection<ExistingMemFunCallRegistration> getExpectationsToRemove() {
      final Collection<ExistingMemFunCallRegistration> expectations = orderPreservingSet();
      for (final String expectation : expectationsToRemove) {
         expectations.add(new ExistingMemFunCallRegistration(expectation));
      }
      return expectations;
   }

   public Collection<String> getExpectationsToAdd() {
      return expectationsToAdd;
   }

   public String getResolutionDesc() {
      return resolutionDesc;
   }

   @Override
   public int getNumOfProblemArguments() {
      return 3;
   }
}
