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
	public void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_TypeResolutionProblem;
	}

	@Override
	public void testMarkerMessage() {
		assertExactlyTheSame("Type 'Type' cannot be resolved.", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 17, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 4, getMarkerLength());
	}

	@Override
	public void testQuickFixMessage() {
		assertExactlyTheSame(CREATE_TYPE_TYPE, getQuickFixMessage(CreateTypeQuickFix.class, CREATE_TYPE_TYPE));
	}

	//struct Type {
	//};
	//
	//void testX() {
	//  Type t;
	//}
	@Override
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(CreateTypeQuickFix.class, CREATE_TYPE_TYPE));
	}

	@Override
	public void testImageNotNull() {
		assertNotNull(getQuickFix(CreateTypeQuickFix.class, CREATE_TYPE_TYPE).getImage());
	}
}
