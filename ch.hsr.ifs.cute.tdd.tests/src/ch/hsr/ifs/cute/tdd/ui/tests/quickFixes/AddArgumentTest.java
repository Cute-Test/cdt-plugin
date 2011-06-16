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
import ch.hsr.ifs.cute.tdd.addArgument.AddArgumentQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class AddArgumentTest extends QuickFixTest {

	private static final String ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT = "Add argument(s) 'int' to match 'foo(int)'";

	//void foo(int) {}
	//void test() {
	//  foo();
	//}
	@Override
	protected void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_InvalidArguments_FREE_HSR;
	}

	public void testMarkerMessage() {
		assertExactlytheSame("Invalid Arguments to foo", getMarkerMessage());
	}

	public void testMarkerOffset() {
		assertEquals("Marker offset", 33, getMarkerOffset());
	}

	public void testMarkerLength() {
		assertEquals("Marker length", 3, getMarkerLength());
	}

	//TODO: non sense
	public void testQuickFixMessage() {
		assertExactlytheSame(ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT, getQuickFixMessage(AddArgumentQuickFix.class, ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT));
	}

	//void foo(int) {}
	//void test() {
	//  foo(_);
	//}
	public void testQuickFixApplying() {
		assertExactlytheSame(getAboveComment(), runQuickFix(AddArgumentQuickFix.class,  ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT));
	}

	public void testImageNotNull() {
		assertNotNull(getQuickFix(AddArgumentQuickFix.class, ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT).getImage());
	}
}
