package ch.hsr.ifs.cute.mockator.incompleteclass.checker;

import static ch.hsr.ifs.cute.mockator.base.i18n.I18N.MemberFunctionsToImplementTitle;

import java.util.Collection;
import java.util.stream.Collectors;

import ch.hsr.ifs.iltis.core.resources.StringUtil;

import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;


class MissingMemFunSignaturesGenerator {

    private static final String      HTML_NEW_LINE    = "<br/>";
    private static final String      STATISTIC_HEADER = "<b>%d %s</b>:%s";
    private final Collection<String> missingMemFunSignatures;

    public MissingMemFunSignaturesGenerator(final Collection<MissingMemberFunction> missingMemFuns) {
        missingMemFunSignatures = missingMemFuns.stream().map((memFun) -> memFun.getFunctionSignature()).collect(Collectors.toList());
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
        return htmlize(signatures).stream().collect(Collectors.joining(HTML_NEW_LINE));
    }

    private static Collection<String> htmlize(final Collection<String> signatures) {
        return signatures.stream().map((signature) -> StringUtil.CodeString.escapeHtml(signature)).collect(Collectors.toList());
    }
}
