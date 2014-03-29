package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.text.edits.ReplaceEdit;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.base.tuples.Tuple;
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
  protected final LinkedModeInformation linkedMode;
  protected final CppStandard cppStd;
  private final ChangeEdit changeEdit;
  private final Maybe<String> expectationsName;
  private final AssertionOrder assertOrder;
  private final Pattern prefixPattern;

  public MockObjectLinkedModeSupport(ChangeEdit changeEdit,
      Collection<? extends TestDoubleMemFun> memFuns, CppStandard cppStd,
      AssertionOrder assertOrder, Maybe<String> expectationsVectorName) {
    this.changeEdit = changeEdit;
    this.memFuns = memFuns;
    this.cppStd = cppStd;
    this.assertOrder = assertOrder;
    this.expectationsName = expectationsVectorName;
    linkedMode = new LinkedModeInformation();
    prefixPattern = createPrefixPattern(cppStd);
  }

  private static Pattern createPrefixPattern(CppStandard cppStd) {
    return Pattern
        .compile(String.format("^(?:\\%s\\s*,\\s*)(.*)", cppStd.getExpectationDelimiter()));
  }

  @Override
  public LinkedModeInformation createLinkedModeInfo() {
    List<OffsetAndLength> expectationPositions = getFunSignaturePositions();
    addPositions(expectationPositions);
    addFunSignatureProposals(expectationPositions);
    addAssertPositionsAndProposals();
    return linkedMode;
  }

  protected abstract void addFunSignatureProposals(Collection<OffsetAndLength> expectationPositions);

  protected List<OffsetAndLength> getFunSignaturePositions() {
    List<OffsetAndLength> offsetsAndLengths = list();
    Maybe<Pair<Integer, String>> expectationInfos = getExpectationVectorInfo();

    if (expectationInfos.isSome()) {
      collectPositionsWithNewVector(offsetsAndLengths, expectationInfos.get());
    } else {
      collectPositionsInVector(offsetsAndLengths);
    }

    return offsetsAndLengths;
  }

  protected abstract void collectPositionsWithNewVector(List<OffsetAndLength> offsetsAndLengths,
      Pair<Integer, String> expectationInfos);

  private void collectPositionsInVector(List<OffsetAndLength> offsetsAndLengths) {
    for (ReplaceEdit optEdit : getEditForCallRegistration()) {
      int beginOfExpectations = optEdit.getOffset();
      String editText = optEdit.getText() + cppStd.getExpectationDelimiter();
      Matcher matcher = prefixPattern.matcher(editText);

      if (matcher.matches()) {
        beginOfExpectations += matcher.start(0);
      }

      collectVectorInsidePositions(offsetsAndLengths, editText, beginOfExpectations);
    }
  }

  protected abstract void collectVectorInsidePositions(List<OffsetAndLength> offsetsAndLengths,
      String editText, int beginOfExpectations);

  private void addAssertPositionsAndProposals() {
    for (OffsetAndLength optOffset : getAssertOffsetAndLength()) {
      addAssertPosition(optOffset);
      addAssertProposals(optOffset);
    }
  }

  private void addAssertProposals(OffsetAndLength assertPosition) {
    List<Proposal> proposals = list();

    for (AssertKind each : AssertKind.getAssertProposals()) {
      proposals.add(createNewProposal(each.toString()));
    }

    linkedMode.addProposal(_1(assertPosition), proposals.toArray(new Proposal[proposals.size()]));
  }

  protected Proposal createNewProposal(String text) {
    return new Proposal(text, CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION), 0);
  }

  private void addAssertPosition(OffsetAndLength assertPosition) {
    linkedMode.addPosition(_1(assertPosition), _2(assertPosition));
  }

  private void addPositions(Collection<OffsetAndLength> initializerPositions) {
    for (OffsetAndLength pos : initializerPositions) {
      addAssertPosition(pos);
    }
  }

  private Maybe<OffsetAndLength> getAssertOffsetAndLength() {
    for (Integer optAssertPos : getAssertPosition()) {
      int assertLength = assertOrder.getAssertionCommand().length();
      return maybe(new OffsetAndLength(optAssertPos, assertLength));
    }

    return none();
  }

  private Maybe<Integer> getAssertPosition() {
    for (String optChangeEditText : getChangeEditText()) {
      Matcher m = getAssertPattern().matcher(optChangeEditText);

      if (m.find())
        return maybe(getChangeEditOffset().get() + m.start());
    }

    return none();
  }

  private Maybe<String> getChangeEditText() {
    for (String optName : expectationsName)
      return changeEdit.getText(optName);

    return none();
  }

  protected Maybe<Integer> getChangeEditOffset() {
    for (String optName : expectationsName)
      return maybe(changeEdit.getOffset(optName));

    return none();
  }

  private Pattern getAssertPattern() {
    String assertCommand = assertOrder.getAssertionCommand();
    String regex =
        String.format("%s\\s*\\(\\s*(%s\\s*,.*)|(.*,\\s*%s\\s*)\\)", assertCommand,
            expectationsName.get(), expectationsName.get());
    return Pattern.compile(regex, Pattern.MULTILINE);
  }

  private Maybe<Pair<Integer, String>> getExpectationVectorInfo() {
    for (String optChangeText : getChangeEditText()) {
      Matcher m = getExpectationsVectorPattern().matcher(optChangeText);

      if (m.find()) {
        int startPos = m.start(1);
        String callSequenceVectorText = m.group(1);
        return maybe(Tuple.from(startPos, callSequenceVectorText));
      }
    }

    return none();
  }

  private Pattern getExpectationsVectorPattern() {
    String regex = expectationsName.get() + "\\s*\\+?=\\s*([^;]*);$";
    return Pattern.compile(regex, Pattern.MULTILINE);
  }

  private Maybe<ReplaceEdit> getEditForCallRegistration() {
    Pattern pushBackMatcher =
        Pattern.compile("\\." + MockatorConstants.PUSH_BACK + "\\s*\\(\\s*"
            + MockatorConstants.CALL);
    return changeEdit.getNonMatchingReplaceEdit(pushBackMatcher);
  }

  protected static class OffsetAndLength extends Pair<Integer, Integer> {
    public OffsetAndLength(Integer offset, Integer length) {
      super(offset, length);
    }
  }
}
