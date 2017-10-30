package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;


class FunArgumentsLinkedEditMode extends MockObjectLinkedModeSupport {

   private static final Pattern EACH_FUN_ARG = Pattern.compile("[^,](?:[^,]*[^,\\s])?");

   public FunArgumentsLinkedEditMode(final ChangeEdit changeEdit, final Collection<? extends TestDoubleMemFun> memFuns, final CppStandard cppStd,
         final AssertionOrder assertOrder, final Optional<String> expectationsName) {
      super(changeEdit, memFuns, cppStd, assertOrder, expectationsName);
   }

   @Override
   protected void collectPositionsWithNewVector(final List<OffsetAndLength> offsetsAndLengths, final Pair<Integer, String> expectationInfo) {
      getChangeEditOffset().ifPresent((editOffset) -> {
         final int posOfExpectations = _1(expectationInfo);
         final int beginOfExpectations = posOfExpectations + editOffset;
         collectVectorInsidePositions(offsetsAndLengths, _2(expectationInfo), beginOfExpectations);
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
