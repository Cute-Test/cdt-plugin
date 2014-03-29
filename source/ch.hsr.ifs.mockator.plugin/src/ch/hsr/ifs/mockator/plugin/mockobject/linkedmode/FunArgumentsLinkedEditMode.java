package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;

class FunArgumentsLinkedEditMode extends MockObjectLinkedModeSupport {
  private static final Pattern EACH_FUN_ARG = Pattern.compile("[^,](?:[^,]*[^,\\s])?");

  public FunArgumentsLinkedEditMode(ChangeEdit changeEdit,
      Collection<? extends TestDoubleMemFun> memFuns, CppStandard cppStd,
      AssertionOrder assertOrder, Maybe<String> expectationsName) {
    super(changeEdit, memFuns, cppStd, assertOrder, expectationsName);
  }

  @Override
  protected void collectPositionsWithNewVector(List<OffsetAndLength> offsetsAndLengths,
      Pair<Integer, String> expectationInfo) {
    for (Integer optEditOffset : getChangeEditOffset()) {
      int posOfExpectations = _1(expectationInfo);
      int beginOfExpectations = posOfExpectations + optEditOffset;
      collectVectorInsidePositions(offsetsAndLengths, _2(expectationInfo), beginOfExpectations);
    }
  }

  @Override
  protected void collectVectorInsidePositions(List<OffsetAndLength> offsetsAndLengths,
      String editText, int beginOfExpectations) {
    Matcher expectationsMatcher = getMatcherForFunExpectations(editText);

    while (expectationsMatcher.find()) {
      int expectationIdx = beginOfExpectations + expectationsMatcher.start(0);
      String expectation = expectationsMatcher.group(0);
      Matcher funArgsMatcher = getMatcherForAllFunArgs(expectation);

      if (!funArgsMatcher.find()) {
        continue;
      }

      int posOfFunArgs = expectationIdx + funArgsMatcher.start(1);
      Matcher funArgMatcher = EACH_FUN_ARG.matcher(funArgsMatcher.group(1));

      while (funArgMatcher.find()) {
        if (funArgMatcher.group().trim().isEmpty()) {
          continue;
        }
        int funArgOffset = posOfFunArgs + funArgMatcher.start();
        int funArgLength = funArgMatcher.end() - funArgMatcher.start();
        offsetsAndLengths.add(new OffsetAndLength(funArgOffset, funArgLength));
      }
    }
  }

  private Matcher getMatcherForAllFunArgs(String funCallRegistration) {
    return cppStd.getAllFunArgsPattern().matcher(funCallRegistration);
  }

  private Matcher getMatcherForFunExpectations(String expectationVector) {
    return cppStd.getFunExpectationsPattern().matcher(expectationVector);
  }

  @Override
  protected void addFunSignatureProposals(Collection<OffsetAndLength> expectationPositions) {
    // no proposals yet for function arguments
  }
}
