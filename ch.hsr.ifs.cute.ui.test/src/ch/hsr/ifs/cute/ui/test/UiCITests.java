/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import ch.hsr.ifs.cute.ui.test.checkers.UnregisteredTestFunctionCheckerTest;

public class UiCITests extends TestSuite {

	public UiCITests() {
		super("CUTE Plugin UI Build Server Tests");
		addTest(CuteSuiteWizardHandlerTest.suite());
		// TODO: whats that supposted to mean? investigate! (lfelber)
		// addTest(SourceActionsTest.suite()); //Don't run on the build server
		addTestSuite(UnregisteredTestFunctionCheckerTest.class);
	}

	public static Test suite() throws Exception {
		return new UiCITests();
	}

}
