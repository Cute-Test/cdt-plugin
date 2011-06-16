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

import org.junit.Test;

import ch.hsr.ifs.cute.tdd.createtype.CreateTypeQuickFix;

@SuppressWarnings("restriction")
public class CreateClassLMTest {

	@Test
	public void testNumberOfProposals() {
		assertEquals(3, CreateTypeQuickFix.getTypeProposals().length);
	}

	@Test
	public void testClassTypeProposal() {
		assertEquals(CreateTypeQuickFix.getTypeProposals()[0].getDisplayString(), "class");
	}

	@Test
	public void testStructTypeProposal() {
		assertEquals(CreateTypeQuickFix.getTypeProposals()[1].getDisplayString(), "struct");
	}

	@Test
	public void testEnumTypeProposal() {
		assertEquals(CreateTypeQuickFix.getTypeProposals()[2].getDisplayString(), "enum");
	}

}
