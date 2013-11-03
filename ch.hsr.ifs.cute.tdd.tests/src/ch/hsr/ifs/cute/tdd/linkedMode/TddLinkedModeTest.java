/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.linkedMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;

@SuppressWarnings("restriction")
public class TddLinkedModeTest {

	private LinkedModeInformation lmi;

	@Before
	public void setUp() {
		lmi = new LinkedModeInformation();
	}

	@Test
	public void testGroupsExist() {
		assertTrue(lmi.getGroups() != null);
	}

	@Test
	public void testGroupsAreEmpty() {
		assertEquals(0, lmi.getGroups().size());
	}

	@Test
	public void testAddPosition() {
		lmi.addPosition(42, 3);
		assertEquals(1, lmi.getGroups().size());
	}

	@Test
	public void testCorrectPositionAdded() {
		lmi.addPosition(42, 3);
		assertSamePosition(42, 3, lmi.getGroups().get(0));
	}

	@Test
	public void testPositionOrder() {
		lmi.addPosition(20, 2);
		lmi.addPosition(10, 1);
		lmi.addPosition(30, 3);
		assertSamePosition(20, 2, lmi.getGroups().get(0));
		assertSamePosition(10, 1, lmi.getGroups().get(1));
		assertSamePosition(30, 3, lmi.getGroups().get(2));
	}

	@Test
	public void testExitPosition() {
		lmi.setExit(42);
		assertEquals(42, lmi.getExitOffset());
	}

	@Test
	public void testGetGroupByOffset() {
		lmi.addPosition(20, 2);
		lmi.addPosition(10, 1);
		lmi.addPosition(30, 3);
		assertSamePosition(10, 1, lmi.getGroup(10));
	}

	@Test
	public void testAddSiblingPosition() {
		lmi.addPosition(0, 3);
		lmi.addPosition(3, 3, 0);
		assertEquals(2, lmi.getGroups().get(0).getPositions().length);
	}

	@Test
	public void testAddOneProposal() {
		lmi.addPosition(0, 3);
		lmi.addProposal(0, new Proposal[] { new Proposal("class", null, 0) });
		assertEquals(1, lmi.getGroups().get(0).getProposals().length);
	}

	@Test
	public void testNoGroupIsAddedIfSiblingIsAdded() {
		lmi.addPosition(0, 3);
		lmi.addPosition(3, 3, 0);
		assertEquals(1, lmi.getGroups().size());
	}

	@Test(expected = RuntimeException.class)
	public void testInsertionOfSiblingWithOtherLength() {
		lmi.addPosition(0, 3);
		lmi.addPosition(3, 4, 0);
	}

	@Test(expected = RuntimeException.class)
	public void testAddPositionToNonexistentSibling() {
		lmi.addPosition(3, 3, 0);
	}

	private void assertSamePosition(int offset, int length, LinkedProposalPositionGroup group) {
		assertEquals(offset, group.getPositions()[0].getOffset());
		assertEquals(length, group.getPositions()[0].getLength());
	}
}
