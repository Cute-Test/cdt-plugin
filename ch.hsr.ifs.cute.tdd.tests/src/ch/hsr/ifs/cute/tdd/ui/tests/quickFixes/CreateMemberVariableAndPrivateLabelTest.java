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
import ch.hsr.ifs.cute.tdd.createvariable.CreateMemberVariableQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class CreateMemberVariableAndPrivateLabelTest extends QuickFixTest {

	private static final String CREATE_MEMBER_VARIABLE_I = "Create member variable i";

	//struct S{
	//    S() : i(5){
	//    }
	//};
	@Override
	protected void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_MemberVariableResolutionProblem_HSR;
	}

	public void testMarkerMessage() {
		assertExactlyTheSame("Cannot resolve member variable", getMarkerMessage());
	}

	public void testMarkerOffset() {
		assertEquals("Marker offset", 20, getMarkerOffset());
	}

	public void testMarkerLength() {
		assertEquals("Marker length", 1, getMarkerLength());
	}

	public void testQuickFixMessage() {
		assertExactlyTheSame(CREATE_MEMBER_VARIABLE_I, getQuickFixMessage(CreateMemberVariableQuickFix.class, CREATE_MEMBER_VARIABLE_I));
	}

	//struct S{
	//    S() : i(5){
	//    }
	//
	//private:
	//    int i;
	//};
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(CreateMemberVariableQuickFix.class, CREATE_MEMBER_VARIABLE_I));
	}

	public void testImageNotNull() {
		assertNotNull(getQuickFix(CreateMemberVariableQuickFix.class, CREATE_MEMBER_VARIABLE_I).getImage());
	}
}
