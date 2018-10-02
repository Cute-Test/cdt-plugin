package ch.hsr.ifs.cute.mockator.infos;

import java.util.Collection;
import java.util.LinkedHashSet;

import ch.hsr.ifs.iltis.core.core.resources.StringUtil;

import ch.hsr.ifs.iltis.cpp.core.collections.StringList;
import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.InfoArgument;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.MessageInfoArgument;

import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.testdouble.support.MemFunSignature;


public class ConsistentExpectationsInfo extends MarkerInfo<ConsistentExpectationsInfo> {

   @MessageInfoArgument(0)
   public String resolutionDescString;

   @InfoArgument
   public StringList expectationsToAdd = StringList.newList();

   @InfoArgument
   public StringList expectationsToRemove = StringList.newList();

   /**
    * Default constructor needed by Framework
    */
   public ConsistentExpectationsInfo() {}

   private static final String HTML_NEW_LINE = "<br/>";

   public void setResolutionDesc(final Collection<MemFunSignature> toRemove, final Collection<MemFunSignature> toAdd) {
      final StringBuilder resolutionDesc = new StringBuilder();

      for (final MemFunSignature sig : toAdd) {
         resolutionDesc.append("+ " + htmlize(sig) + HTML_NEW_LINE);
      }

      for (final MemFunSignature sig : toRemove) {
         resolutionDesc.append("- " + htmlize(sig) + HTML_NEW_LINE);
      }

      resolutionDescString = resolutionDesc.toString();
   }

   private static String htmlize(final MemFunSignature funCallExpectation) {
      return StringUtil.quote(StringUtil.CodeString.escapeHtml(StringUtil.unquote(funCallExpectation.toString())));
   }

   public Collection<ExistingMemFunCallRegistration> getExpectationsToRemove() {
      final Collection<ExistingMemFunCallRegistration> expectations = new LinkedHashSet<>();
      for (final String expectation : expectationsToRemove) {
         expectations.add(new ExistingMemFunCallRegistration(expectation));
      }
      return expectations;
   }

}
