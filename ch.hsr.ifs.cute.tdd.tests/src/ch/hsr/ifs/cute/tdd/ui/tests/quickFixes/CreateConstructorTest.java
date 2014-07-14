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
	public void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_MissingConstructorResolutionProblem;
	}

	@Override
	public void testMarkerMessage() {
		assertExactlyTheSame("No such constructor for type 'A'.", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 33, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 1, getMarkerLength());
	}

	@Override
	public void testQuickFixMessage() {
		assertExactlyTheSame(CREATE_CONSTRUCTOR_A, getQuickFixMessage(CreateConstructorCreationQuickFix.class, CREATE_CONSTRUCTOR_A));
	}

	//struct A {
	//	A(const int& i) {
	//	}
	//};
	//void testX() {
	//  A a(3);
	//}
	@Override
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(CreateConstructorCreationQuickFix.class, CREATE_CONSTRUCTOR_A));
	}

	@Override
	public void testImageNotNull() {
		assertNotNull(getQuickFix(CreateConstructorCreationQuickFix.class, CREATE_CONSTRUCTOR_A).getImage());
	}
}
