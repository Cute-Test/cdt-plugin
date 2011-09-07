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
import ch.hsr.ifs.cute.tdd.createfunction.quickfixes.NormalFreeFunctionCreationQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class CreateFunctionTest extends QuickFixTest {

	private static final String CREATE_FREE_FUNCTION_FOO = "Create function foo";

	//void testX() {
	//  foo();
	//}
	@Override
	protected void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_FunctionResolutionProblem_HSR;
	}

	@Override
	public void testMarkerMessage() {
		assertExactlyTheSame("Could not resolve function foo", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 17, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 3, getMarkerLength());
	}

	@Override
	public void testQuickFixMessage() {
		assertExactlyTheSame(CREATE_FREE_FUNCTION_FOO, getQuickFixMessage(NormalFreeFunctionCreationQuickFix.class, CREATE_FREE_FUNCTION_FOO));
	}

	//void foo()
	//{
	//}
	//
	//void testX() {
	//  foo();
	//}
	@Override
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(NormalFreeFunctionCreationQuickFix.class, CREATE_FREE_FUNCTION_FOO));
	}

	@Override
	public void testImageNotNull() {
		assertNotNull(getQuickFix(NormalFreeFunctionCreationQuickFix.class, CREATE_FREE_FUNCTION_FOO).getImage());
	}
}
