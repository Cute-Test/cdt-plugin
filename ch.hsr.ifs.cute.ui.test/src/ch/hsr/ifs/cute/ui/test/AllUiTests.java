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
import ch.hsr.ifs.cute.ui.test.sourceactions.SourceActionsTest;

public class AllUiTests extends TestSuite{

	public AllUiTests() {
		super("CUTE Plugin All UI Tests"); //$NON-NLS-1$
		addTest(CuteSuiteWizardHandlerTest.suite());
		addTest(SourceActionsTest.suite());
		addTestSuite(UnregisteredTestFunctionCheckerTest.class);
	}

	public static Test suite() throws Exception {
		return new AllUiTests();
	}

}
