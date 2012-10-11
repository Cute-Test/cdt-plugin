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
import ch.hsr.ifs.cute.tdd.changevisibility.ChangeVisibilityQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class ChangeVisibilityTest extends QuickFixTest {

	private static final String CHANGE_VISIBILITY_OF_MEMBER = "Change visibility of 'member'";

	//class Type {
	//  void member(){}
	//};
	//void test() {
	//  Type t;
	//  t.member();
	//}
	@Override
	public void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_PrivateMethodChecker_HSR;
	}

	@Override
	public void testMarkerMessage() {
		assertExactlyTheSame("member is not visible", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 62, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 6, getMarkerLength());
	}

	@Override
	public void testQuickFixMessage() {
		assertExactlyTheSame(CHANGE_VISIBILITY_OF_MEMBER, getQuickFixMessage(ChangeVisibilityQuickFix.class, CHANGE_VISIBILITY_OF_MEMBER));
	}

	//class Type {
	//public:
	//	void member() {
	//	}
	//};
	//void test() {
	//  Type t;
	//  t.member();
	//}
	@Override
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(ChangeVisibilityQuickFix.class, CHANGE_VISIBILITY_OF_MEMBER));
	}

	@Override
	public void testImageNotNull() {
		//no image here
	}
}
