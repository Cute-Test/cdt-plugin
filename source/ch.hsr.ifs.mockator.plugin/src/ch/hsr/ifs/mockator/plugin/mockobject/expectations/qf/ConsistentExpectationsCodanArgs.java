package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;
import static ch.hsr.ifs.mockator.plugin.base.util.HtmlUtil.escapeHtml;
import static ch.hsr.ifs.mockator.plugin.base.util.StringUtil.quote;
import static ch.hsr.ifs.mockator.plugin.base.util.StringUtil.unquote;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.CodanArguments;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;


class ConsistentExpectationsCodanArgs extends CodanArguments {

   private static final String      HTML_NEW_LINE       = "<br/>";
   private static final String      SIGNATURE_DELIMITER = "\"";
   private final Collection<String> expectationsToRemove;
   private final Collection<String> expectationsToAdd;
   private final String             resolutionDesc;

   public ConsistentExpectationsCodanArgs(IMarker marker) {
      String[] problemArgs = getProblemArguments(marker);
      expectationsToRemove = split(problemArgs[0]);
      expectationsToAdd = split(problemArgs[1]);
      resolutionDesc = problemArgs[2];
   }

   public ConsistentExpectationsCodanArgs(Collection<MemFunSignature> toRemove, Collection<MemFunSignature> toAdd) {
      expectationsToRemove = toStrings(toRemove);
      expectationsToAdd = toStrings(toAdd);
      resolutionDesc = getResolutionDesc(toRemove, toAdd);
   }

   private static Collection<String> toStrings(Collection<MemFunSignature> signatures) {
      return map(signatures, new F1<MemFunSignature, String>() {

         @Override
         public String apply(MemFunSignature expectation) {
            return expectation.toString();
         }
      });
   }

   private static String getResolutionDesc(Collection<MemFunSignature> toRemove, Collection<MemFunSignature> toAdd) {
      StringBuilder resolutionDesc = new StringBuilder();

      for (MemFunSignature sig : toAdd) {
         resolutionDesc.append("+ " + htmlize(sig) + HTML_NEW_LINE);
      }

      for (MemFunSignature sig : toRemove) {
         resolutionDesc.append("- " + htmlize(sig) + HTML_NEW_LINE);
      }

      return resolutionDesc.toString();
   }

   private static String htmlize(MemFunSignature funCallExpectation) {
      return quote(escapeHtml(unquote(funCallExpectation.toString())));
   }

   private static List<String> split(String expectations) {
      if (expectations.trim().isEmpty()) return list();
      return list(expectations.split(SIGNATURE_DELIMITER));
   }

   @Override
   public Object[] toArray() {
      return array(toString(expectationsToRemove), toString(expectationsToAdd), resolutionDesc);
   }

   private static String toString(Collection<String> expectations) {
      return StringUtil.join(expectations, SIGNATURE_DELIMITER);
   }

   public Collection<ExistingMemFunCallRegistration> getExpectationsToRemove() {
      Collection<ExistingMemFunCallRegistration> expectations = orderPreservingSet();
      for (String expectation : expectationsToRemove) {
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
