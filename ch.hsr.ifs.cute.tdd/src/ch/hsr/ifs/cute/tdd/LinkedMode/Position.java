/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.LinkedMode;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;

@SuppressWarnings("restriction")
public class Position extends PositionInformation {

	private final int offset;
	private final int length;
	private final int rank;

	public Position(int offset, int length) {
		this(offset, length, 0);
	}

	public Position(int offset, int length, int rank) {
		this.offset = offset;
		this.length = length;
		this.rank = rank;
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
		return rank;
	}

}