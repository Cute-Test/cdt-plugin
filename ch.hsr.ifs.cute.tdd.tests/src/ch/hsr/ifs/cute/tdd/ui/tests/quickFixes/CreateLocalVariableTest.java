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
import ch.hsr.ifs.cute.tdd.createvariable.CreateLocalVariableQuickFix;
import ch.hsr.ifs.cute.tdd.ui.tests.QuickFixTest;

public class CreateLocalVariableTest extends QuickFixTest {

	private static final String CREATE_LOCAL_VARIABLE_LOCAL = "Create local variable local";

	//void testX() {
	//  int i = local;
	//}
	@Override
	public void getCode() {
	}

	@Override
	protected String getId() {
		return TddErrorIdCollection.ERR_ID_VariableResolutionProblem_HSR;
	}

	@Override
	public void testMarkerMessage() {
		assertExactlyTheSame("Symbol 'local' could not be resolved", getMarkerMessage());
	}

	@Override
	public void testMarkerOffset() {
		assertEquals("Marker offset", 25, getMarkerOffset());
	}

	@Override
	public void testMarkerLength() {
		assertEquals("Marker length", 5, getMarkerLength());
	}

	@Override
	public void testQuickFixMessage() {
		assertExactlyTheSame(CREATE_LOCAL_VARIABLE_LOCAL, getQuickFixMessage(CreateLocalVariableQuickFix.class, CREATE_LOCAL_VARIABLE_LOCAL));
	}

	//void testX() {
	//	int local;
	//	int i = local;
	//}
	@Override
	public void testQuickFixApplying() {
		assertExactlyTheSame(getAboveComment(), runQuickFix(CreateLocalVariableQuickFix.class, CREATE_LOCAL_VARIABLE_LOCAL));
	}

	@Override
	public void testImageNotNull() {
		assertNotNull(getQuickFix(CreateLocalVariableQuickFix.class, CREATE_LOCAL_VARIABLE_LOCAL).getImage());
	}
}
