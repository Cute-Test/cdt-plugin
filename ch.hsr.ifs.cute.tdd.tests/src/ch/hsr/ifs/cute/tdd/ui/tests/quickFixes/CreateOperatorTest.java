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
import ch.hsr.ifs.cute.tdd.createfunction.quickfixes.MemberOperatorCreationQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class CreateOperatorTest extends QuickFixTest {

	private static final String CREATE_OPERATOR_IN_TYPE_A = "Create operator++ in type A";

	//struct A {
	//};
	//void testX() {
	//    A a;
	//    a++;
	//}
	@Override
	protected void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_OperatorResolutionProblem_HSR;
	}

	public void testMarkerMessage() {
		assertExactlytheSame("Cannot resolve operator++ in type A", getMarkerMessage());
	}

// TODO: I'd propose to put the marker only on the operator
//	public void testMarkerOffset() {
//		assertEquals("Marker offset", 39, getMarkerOffset());
//	}
//
//	public void testMarkerLength() {
//		assertEquals("Marker length", 2, getMarkerLength());
//	}

	public void testQuickFixMessage() {
		assertExactlytheSame(CREATE_OPERATOR_IN_TYPE_A, getQuickFixMessage(MemberOperatorCreationQuickFix.class, CREATE_OPERATOR_IN_TYPE_A));
	}

	//struct A {
	//    void operator++(int)
	//    {
	//    }
	//};
	//void testX() {
	//    A a;
	//    a++;
	//}
	public void testQuickFixApplying() {
		assertExactlytheSame(getAboveComment(), runQuickFix(MemberOperatorCreationQuickFix.class, CREATE_OPERATOR_IN_TYPE_A));
	}

	public void testImageNotNull() {
		assertNotNull(getQuickFix(MemberOperatorCreationQuickFix.class, CREATE_OPERATOR_IN_TYPE_A).getImage());
	}

	@Override
	public void testMarkerOffset() {
		//not tested here
		
	}

	@Override
	public void testMarkerLength() {
		//not tested here
	}
}