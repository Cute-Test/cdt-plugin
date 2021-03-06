package ch.hsr.ifs.cute.mockator.mockobject.linkedmode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.project.properties.AssertionOrder;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;


class FunArgumentsLinkedEditMode extends MockObjectLinkedModeSupport {

    private static final Pattern EACH_FUN_ARG = Pattern.compile("[^,](?:[^,]*[^,\\s])?");

    public FunArgumentsLinkedEditMode(final ChangeEdit changeEdit, final Collection<? extends TestDoubleMemFun> memFuns, final CppStandard cppStd,
                                      final AssertionOrder assertOrder, final Optional<String> expectationsName) {
        super(changeEdit, memFuns, cppStd, assertOrder, expectationsName);
    }

    @Override
    protected void collectPositionsWithNewVector(final List<OffsetAndLength> offsetsAndLengths, final ExpectationVectorInfo expectationInfo) {
        getChangeEditOffset().ifPresent((editOffset) -> {
            final int posOfExpectations = expectationInfo.getStartPosition();
            final int beginOfExpectations = posOfExpectations + editOffset;
            collectVectorInsidePositions(offsetsAndLengths, expectationInfo.getEditText(), beginOfExpectations);
        });
    }

    @Override
    protected void collectVectorInsidePositions(final List<OffsetAndLength> offsetsAndLengths, final String editText, final int beginOfExpectations) {
        final Matcher expectationsMatcher = getMatcherForFunExpectations(editText);

        while (expectationsMatcher.find()) {
            final int expectationIdx = beginOfExpectations + expectationsMatcher.start(0);
            final String expectation = expectationsMatcher.group(0);
            final Matcher funArgsMatcher = getMatcherForAllFunArgs(expectation);

            if (!funArgsMatcher.find()) {
                continue;
            }

            final int posOfFunArgs = expectationIdx + funArgsMatcher.start(1);
            final Matcher funArgMatcher = EACH_FUN_ARG.matcher(funArgsMatcher.group(1));

            while (funArgMatcher.find()) {
                if (funArgMatcher.group().trim().isEmpty()) {
                    continue;
                }
                final int funArgOffset = posOfFunArgs + funArgMatcher.start();
                final int funArgLength = funArgMatcher.end() - funArgMatcher.start();
                offsetsAndLengths.add(new OffsetAndLength(funArgOffset, funArgLength));
            }
        }
    }

    private Matcher getMatcherForAllFunArgs(final String funCallRegistration) {
        return cppStd.getAllFunArgsPattern().matcher(funCallRegistration);
    }

    private Matcher getMatcherForFunExpectations(final String expectationVector) {
        return cppStd.getFunExpectationsPattern().matcher(expectationVector);
    }

    @Override
    protected void addFunSignatureProposals(final Collection<OffsetAndLength> expectationPositions) {
        // no proposals yet for function arguments
    }
}
