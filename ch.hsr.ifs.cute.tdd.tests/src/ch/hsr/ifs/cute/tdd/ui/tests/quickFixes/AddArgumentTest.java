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
	public void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_InvalidArguments_FREE;
	}

	@Override
	public void testMarkerMessage() {
		assertExactlyTheSame("Invalid arguments for function 'foo'.", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 33, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 3, getMarkerLength());
	}

	// TODO: non sense
	@Override
	public void testQuickFixMessage() {
		assertExactlyTheSame(ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT, getQuickFixMessage(AddArgumentQuickFix.class, ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT));
	}

	//void foo(int) {}
	//void test() {
	//	foo (_);
	//}
	@Override
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(AddArgumentQuickFix.class, ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT));
	}

	@Override
	public void testImageNotNull() {
		assertNotNull(getQuickFix(AddArgumentQuickFix.class, ADD_ARGUMENT_S_INT_TO_MATCH_FOO_INT).getImage());
	}
}
