package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.text.edits.ReplaceEdit;

import ch.hsr.ifs.iltis.core.core.data.AbstractPair;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.asserteq.AssertKind;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInformation;


@SuppressWarnings("restriction")
abstract class MockObjectLinkedModeSupport implements LinkedModeInfoCreater {

   protected final Collection<? extends TestDoubleMemFun> memFuns;
   protected final LinkedModeInformation                  linkedMode;
   protected final CppStandard                            cppStd;
   private final ChangeEdit                               changeEdit;
   private final Optional<String>                         expectationsName;
   private final AssertionOrder                           assertOrder;
   private final Pattern                                  prefixPattern;

   public MockObjectLinkedModeSupport(final ChangeEdit changeEdit, final Collection<? extends TestDoubleMemFun> memFuns, final CppStandard cppStd,
                                      final AssertionOrder assertOrder, final Optional<String> expectationsVectorName) {
      this.changeEdit = changeEdit;
      this.memFuns = memFuns;
      this.cppStd = cppStd;
      this.assertOrder = assertOrder;
      expectationsName = expectationsVectorName;
      linkedMode = new LinkedModeInformation();
      prefixPattern = createPrefixPattern(cppStd);
   }

   private static Pattern createPrefixPattern(final CppStandard cppStd) {
      return Pattern.compile(String.format("^(?:\\%s\\s*,\\s*)(.*)", cppStd.getExpectationDelimiter()));
   }

   @Override
   public LinkedModeInformation createLinkedModeInfo() {
      final List<OffsetAndLength> expectationPositions = getFunSignaturePositions();
      addPositions(expectationPositions);
      addFunSignatureProposals(expectationPositions);
      addAssertPositionsAndProposals();
      return linkedMode;
   }

   protected abstract void addFunSignatureProposals(Collection<OffsetAndLength> expectationPositions);

   protected List<OffsetAndLength> getFunSignaturePositions() {
      final List<OffsetAndLength> offsetsAndLengths = new ArrayList<>();
      final Optional<ExpectationVectorInfo> expectationInfos = getExpectationVectorInfo();

      if (expectationInfos.isPresent()) {
         collectPositionsWithNewVector(offsetsAndLengths, expectationInfos.get());
      } else {
         collectPositionsInVector(offsetsAndLengths);
      }

      return offsetsAndLengths;
   }

   protected abstract void collectPositionsWithNewVector(List<OffsetAndLength> offsetsAndLengths, ExpectationVectorInfo expectationInfos);

   private void collectPositionsInVector(final List<OffsetAndLength> offsetsAndLengths) {
      getEditForCallRegistration().ifPresent((edit) -> {
         int beginOfExpectations = edit.getOffset();
         final String editText = edit.getText() + cppStd.getExpectationDelimiter();
         final Matcher matcher = prefixPattern.matcher(editText);

         if (matcher.matches()) {
            beginOfExpectations += matcher.start(0);
         }

         collectVectorInsidePositions(offsetsAndLengths, editText, beginOfExpectations);
      });
   }

   protected abstract void collectVectorInsidePositions(List<OffsetAndLength> offsetsAndLengths, String editText, int beginOfExpectations);

   private void addAssertPositionsAndProposals() {
      getAssertOffsetAndLength().ifPresent((offset) -> {
         addAssertPosition(offset);
         addAssertProposals(offset);
      });
   }

   private void addAssertProposals(final OffsetAndLength assertPosition) {
      final List<Proposal> proposals = new ArrayList<>();

      for (final AssertKind each : AssertKind.getAssertProposals()) {
         proposals.add(createNewProposal(each.toString()));
      }

      linkedMode.addProposal(assertPosition.offset(), proposals.toArray(new Proposal[proposals.size()]));
   }

   protected Proposal createNewProposal(final String text) {
      return new Proposal(text, CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION), 0);
   }

   private void addAssertPosition(final OffsetAndLength assertPosition) {
      linkedMode.addPosition(assertPosition.offset(), assertPosition.length());
   }

   private void addPositions(final Collection<OffsetAndLength> initializerPositions) {
      for (final OffsetAndLength pos : initializerPositions) {
         addAssertPosition(pos);
      }
   }

   private Optional<OffsetAndLength> getAssertOffsetAndLength() {
      return getAssertPosition().map(assertPos -> new OffsetAndLength(assertPos, assertOrder.getAssertionCommand().length()));
   }

   private Optional<Integer> getAssertPosition() {
      return getChangeEditText().map(changeEditText -> {
         final Matcher m = getAssertPattern().matcher(changeEditText);
         return m.find() ? getChangeEditOffset().get() + m.start() : null;
      });
   }

   private Optional<String> getChangeEditText() {
      return expectationsName.flatMap(changeEdit::getText);
   }

   protected Optional<Integer> getChangeEditOffset() {
      return expectationsName.map(changeEdit::getOffset);
   }

   private Pattern getAssertPattern() {
      final String assertCommand = assertOrder.getAssertionCommand();
      final String regex = String.format("%s\\s*\\(\\s*(%s\\s*,.*)|(.*,\\s*%s\\s*)\\)", assertCommand, expectationsName.get(), expectationsName
            .get());
      return Pattern.compile(regex, Pattern.MULTILINE);
   }

   private Optional<ExpectationVectorInfo> getExpectationVectorInfo() {
      return getChangeEditText().map(changeText -> {
         final Matcher m = getExpectationsVectorPattern().matcher(changeText);

         if (m.find()) {
            final int startPos = m.start(1);
            final String callSequenceVectorText = m.group(1);
            return new ExpectationVectorInfo(startPos, callSequenceVectorText);
         }
         return null;
      });

   }

   private Pattern getExpectationsVectorPattern() {
      final String regex = expectationsName.get() + "\\s*\\+?=\\s*([^;]*);$";
      return Pattern.compile(regex, Pattern.MULTILINE);
   }

   private Optional<ReplaceEdit> getEditForCallRegistration() {
      final Pattern pushBackMatcher = Pattern.compile("\\." + MockatorConstants.PUSH_BACK + "\\s*\\(\\s*" + MockatorConstants.CALL);
      return changeEdit.getNonMatchingReplaceEdit(pushBackMatcher);
   }

   protected static class OffsetAndLength extends AbstractPair<Integer, Integer> {

      public OffsetAndLength(final Integer offset, final Integer length) {
         super(offset, length);
      }

      public int offset() {
         return first;
      }

      public int length() {
         return second;
      }
   }

   class ExpectationVectorInfo extends AbstractPair<Integer, String> {

      public ExpectationVectorInfo(Integer first, String second) {
         super(first, second);
      }

      public Integer getStartPosition() {
         return first;
      }

      public String getEditText() {
         return second;
      }

   }
}
