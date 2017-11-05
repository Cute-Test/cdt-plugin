package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;
import static ch.hsr.ifs.mockator.plugin.base.i18n.I18N.MemberFunctionsToImplementTitle;

import java.util.Collection;

import ch.hsr.ifs.mockator.plugin.base.util.HtmlUtil;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;

class MissingMemFunSignaturesGenerator {

  private static final String HTML_NEW_LINE = "<br/>";
  private static final String STATISTIC_HEADER = "<b>%d %s</b>:%s";
  private final Collection<String> missingMemFunSignatures;

  public MissingMemFunSignaturesGenerator(final Collection<MissingMemberFunction> missingMemFuns) {
    missingMemFunSignatures = map(missingMemFuns, (memFun) -> memFun.getFunctionSignature());
  }

  public String getSignaturesWithStatistics() {
    final StringBuilder signatures = new StringBuilder();
    addStatisticsHeader(signatures);
    addNewLineSeparatedSignatures(signatures);
    return signatures.toString();
  }

  private void addStatisticsHeader(final StringBuilder signatures) {
    signatures.append(String.format(STATISTIC_HEADER, missingMemFunSignatures.size(), MemberFunctionsToImplementTitle, HTML_NEW_LINE));
  }

  private void addNewLineSeparatedSignatures(final StringBuilder signatures) {
    signatures.append(getFunSignaturesAsMultiLineString(missingMemFunSignatures));
  }

  private static String getFunSignaturesAsMultiLineString(final Collection<String> signatures) {
    return StringUtil.join(htmlize(signatures), HTML_NEW_LINE);
  }

  private static Collection<String> htmlize(final Collection<String> signatures) {
    return map(signatures, (signature) -> HtmlUtil.escapeHtml(signature));
  }
}
