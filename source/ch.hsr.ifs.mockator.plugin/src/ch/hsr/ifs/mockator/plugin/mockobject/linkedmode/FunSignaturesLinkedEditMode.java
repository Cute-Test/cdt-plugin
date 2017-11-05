package ch.hsr.ifs.mockator.plugin.mockobject.linkedmode;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;

@SuppressWarnings("restriction")
class FunSignaturesLinkedEditMode extends MockObjectLinkedModeSupport {

  public FunSignaturesLinkedEditMode(final ChangeEdit edit, final Collection<? extends TestDoubleMemFun> memFuns, final CppStandard cppStd,
      final AssertionOrder assertOrder, final Optional<String> expectationsVectorName) {
    super(edit, memFuns, cppStd, assertOrder, expectationsVectorName);
  }

  @Override
  protected void addFunSignatureProposals(final Collection<OffsetAndLength> initializerPositions) {
    final Proposal[] proposals = getMemFunSignatureProposals();

    for (final OffsetAndLength pos : initializerPositions) {
      linkedMode.addProposal(pos.offset(), proposals);
    }
  }

  private Proposal[] getMemFunSignatureProposals() {
    final List<Proposal> proposals = list();

    for (final TestDoubleMemFun memFun : memFuns) {
      proposals.add(createNewProposal(getFunSignature(memFun)));
    }

    return proposals.toArray(new Proposal[proposals.size()]);
  }

  private static String getFunSignature(final TestDoubleMemFun memFun) {
    return StringUtil.quote(memFun.getFunctionSignature());
  }

  @Override
  protected void collectPositionsWithNewVector(final List<OffsetAndLength> offsetsAndLengths, final Pair<Integer, String> expectationInfo) {
    final Integer posOfCallSpecs = expectationInfo.first();
    final String callSpecText = expectationInfo.second();
    collectVectorInsidePositions(offsetsAndLengths, callSpecText, posOfCallSpecs);
  }

  @Override
  protected void collectVectorInsidePositions(final List<OffsetAndLength> offsetsAndLengths, final String callSpecText, final int posOfCallSpecs) {
    getChangeEditOffset().ifPresent((editOffset) -> {
      final Matcher m = getMatcher(callSpecText);

      while (m.find()) {
        final int beginOfCallSpec = m.start(1);
        final int endOfCallSpec = m.end(1);
        final int expectationOffset = editOffset + posOfCallSpecs + beginOfCallSpec;
        final int expectationLength = endOfCallSpec - beginOfCallSpec;
        offsetsAndLengths.add(new OffsetAndLength(expectationOffset, expectationLength));
      }
    });
  }

  private Matcher getMatcher(final String callSequenceVectorLine) {
    return cppStd.getInitializerPattern().matcher(callSequenceVectorLine);
  }
}
