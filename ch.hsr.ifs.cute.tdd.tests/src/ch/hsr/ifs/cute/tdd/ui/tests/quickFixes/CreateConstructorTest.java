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
import ch.hsr.ifs.cute.tdd.createfunction.quickfixes.CreateConstructorCreationQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class CreateConstructorTest extends QuickFixTest {

	private static final String CREATE_CONSTRUCTOR_A = "Create constructor A";

	//struct A {
	//};
	//void testX() {
	//  A a(3);
	//}
	@Override
	protected void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_MissingConstructorResolutionProblem_HSR;
	}

	public void testMarkerMessage() {
		assertExactlytheSame("No such constructor for type A", getMarkerMessage());
	}

	public void testMarkerOffset() {
		assertEquals("Marker offset", 33, getMarkerOffset());
	}

	public void testMarkerLength() {
		assertEquals("Marker length", 1, getMarkerLength());
	}

	public void testQuickFixMessage() {
		assertExactlytheSame(CREATE_CONSTRUCTOR_A, getQuickFixMessage(CreateConstructorCreationQuickFix.class, CREATE_CONSTRUCTOR_A));
	}

	//struct A {
	//     A(const int & i)
	//    {
	//    }
	//};
	//void testX() {
	//  A a(3);
	//}
	public void testQuickFixApplying() {
		assertExactlytheSame(getAboveComment(), runQuickFix(CreateConstructorCreationQuickFix.class, CREATE_CONSTRUCTOR_A));
	}

	public void testImageNotNull() {
		assertNotNull(getQuickFix(CreateConstructorCreationQuickFix.class, CREATE_CONSTRUCTOR_A).getImage());
	}
}
