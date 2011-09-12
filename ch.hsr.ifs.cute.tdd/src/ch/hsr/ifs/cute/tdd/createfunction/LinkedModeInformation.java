/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;

import ch.hsr.ifs.cute.tdd.LinkedMode.Position;

public class LinkedModeInformation {

	private boolean fileChanged;
	private boolean hasReturnStatment;
	private boolean const1;
	private boolean hasDeclSpec;
	private List<LinkedProposalPositionGroup> groups;
	private int exit = -1;
	
	public LinkedModeInformation() {
		groups = new ArrayList<LinkedProposalPositionGroup>();
	}

	public void setFileChanged(boolean fileChanged) {
		this.fileChanged = fileChanged;
	}

	public void setReturnStatement(boolean hasReturnStatement) {
		this.hasReturnStatment = hasReturnStatement;
	}

	public void setIsConst(boolean const1) {
		this.const1 = const1;
	}

	public void sethasDeclSpec(boolean hasdeclspec) {
		this.hasDeclSpec = hasdeclspec;
	}
	
	public boolean isSameFileChange() {
		return fileChanged;
	}
	
	public boolean getReturnStatment() {
		return hasReturnStatment;
	}
	
	public boolean getConst() {
		return const1;
	}
	
	public boolean getDeclSpec() {
		return hasDeclSpec;
	}

	public void addPosition(int offset, int length) {
		LinkedProposalPositionGroup group = new LinkedProposalPositionGroup("group" + offset); //$NON-NLS-1$
		group.addPosition(new Position(offset, length));
		getGroups().add(group);
	}

	public void addPosition(int offset, int length, int siblingPosition) {
		LinkedProposalPositionGroup group = getGroup(siblingPosition);
		if (group == null)
			throw new RuntimeException(Messages.LinkedModeInformation_1 + siblingPosition);
		if (group.getPositions()[0].getLength() != length)
			throw new RuntimeException(Messages.LinkedModeInformation_2);
		group.addPosition(new Position(offset, length, 1));
	}

	public void addPositions(List<Position> positions) {
		for (Position p : positions) {
			addPosition(p.getOffset(), p.getLength());
		}
	}

	public void addProposal(int offset, Proposal[] proposals) {
		LinkedProposalPositionGroup group = getGroup(offset);
		for (Proposal p : proposals) {
			group.addProposal(p);
		}
	}

	public LinkedProposalPositionGroup getGroup(int offset) {
		for (LinkedProposalPositionGroup group : getGroups()) {
			for (PositionInformation position : group.getPositions()) {
				if (position.getOffset() == offset) {
					return group;
				}
			}
		}
		return null;
	}

	public List<LinkedProposalPositionGroup> getGroups() {
		return groups;
	}

	public int getExitOffset() {
		return exit;
	}

	public void setExit(int offset) {
		this.exit = offset;
	}
	
	public void setGroups(List<LinkedProposalPositionGroup> groups) {
		this.groups = groups;
	}
}
