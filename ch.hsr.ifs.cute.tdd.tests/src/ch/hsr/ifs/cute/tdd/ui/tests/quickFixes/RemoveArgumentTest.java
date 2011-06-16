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

public class RemoveArgumentTest extends QuickFixTest{

	private static final String REMOVE_ARGUMENT_S_DOUBLE_DOUBLE_INT_TO_MATCH_FOO_INT = "Remove argument(s) 'double, double, int' to match 'foo(int)'";

	//void foo(int i) {
	//}
	//void test() {
	//	foo(3, 2.3, 2.3, 2);
	//}
	@Override
	protected void getCode() {
	}
	
	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_InvalidArguments_HSR;
	}


	@Override
	public void testMarkerMessage() {
		assertExactlytheSame("Invalid Arguments to foo", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 35, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 3, getMarkerLength());
	}

	@Override
	public void testQuickFixMessage() {
		assertExactlytheSame(REMOVE_ARGUMENT_S_DOUBLE_DOUBLE_INT_TO_MATCH_FOO_INT, getQuickFixMessage(AddArgumentQuickFix.class, REMOVE_ARGUMENT_S_DOUBLE_DOUBLE_INT_TO_MATCH_FOO_INT));
	}

	//void foo(int i) {
	//}
	//void test() {
	//	foo(3);
	//}
	@Override
	public void testQuickFixApplying() {
	}

	@Override
	public void testImageNotNull() {
		assertNotNull(getQuickFix(AddArgumentQuickFix.class, REMOVE_ARGUMENT_S_DOUBLE_DOUBLE_INT_TO_MATCH_FOO_INT).getImage());
	}

}
