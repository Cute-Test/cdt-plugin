/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests.quickFixes;

import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;
import ch.hsr.ifs.cute.tdd.createtype.CreateTypeQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class CreateClassTest extends QuickFixTest {

	private static final String CREATE_TYPE_TYPE = "Create type 'Type'";

	//void testX() {
	//  Type t;
	//}
	@Override
	protected void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_TypeResolutionProblem_HSR;
	}

	public void testMarkerMessage() {
		assertExactlytheSame("Type 'Type' could not be resolved", getMarkerMessage());
	}

	public void testMarkerOffset() {
		assertEquals("Marker offset", 17, getMarkerOffset());
	}

	public void testMarkerLength() {
		assertEquals("Marker length", 4, getMarkerLength());
	}

	public void testQuickFixMessage() {
		assertExactlytheSame(CREATE_TYPE_TYPE, getQuickFixMessage(CreateTypeQuickFix.class, CREATE_TYPE_TYPE));
	}

	//struct Type
	//{
	//};
	//void testX() {
	//  Type t;
	//}
	public void testQuickFixApplying() {
		assertExactlytheSame(getAboveComment(), runQuickFix(CreateTypeQuickFix.class, CREATE_TYPE_TYPE));
	}

	public void testImageNotNull() {
		assertNotNull(getQuickFix(CreateTypeQuickFix.class, CREATE_TYPE_TYPE).getImage());
	}
}
