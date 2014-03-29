package ch.hsr.ifs.mockator.plugin.refsupport.linkededit;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;

@SuppressWarnings("restriction")
public class LinkedModeInformation {
  private final List<LinkedProposalPositionGroup> groups;

  public LinkedModeInformation() {
    groups = list();
  }

  public void addPosition(int offset, int length) {
    LinkedProposalPositionGroup group = new LinkedProposalPositionGroup("group" + offset);
    group.addPosition(new Position(offset, length));
    groups.add(group);
  }

  public void addProposal(int offset, Proposal[] proposals) {
    LinkedProposalPositionGroup group = getGroup(offset);

    for (Proposal proposal : proposals) {
      group.addProposal(proposal);
    }
  }

  List<LinkedProposalPositionGroup> getGroups() {
    return groups;
  }

  public LinkedProposalPositionGroup getGroup(int offset) {
    for (LinkedProposalPositionGroup group : groups) {
      for (PositionInformation pos : group.getPositions()) {
        if (pos.getOffset() == offset)
          return group;
      }
    }

    return null;
  }

  private static class Position extends PositionInformation {
    private final int offset;
    private final int length;

    public Position(int offset, int length) {
      this.offset = offset;
      this.length = length;
    }

    @Override
    public int getOffset() {
      return offset;
    }

    @Override
    public int getLength() {
      return length;
    }

    @Override
    public int getSequenceRank() {
      return 0;
    }
  }
}
