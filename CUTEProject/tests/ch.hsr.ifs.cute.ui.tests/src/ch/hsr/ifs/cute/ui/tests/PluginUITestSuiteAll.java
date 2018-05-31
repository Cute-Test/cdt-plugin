/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.hsr.ifs.cute.ui.tests.checkers.UnregisteredTestFunctionCheckerTest;
import ch.hsr.ifs.cute.ui.tests.sourceactions.SourceActionsTests;


@RunWith(Suite.class)
//@formatter:off
@Suite.SuiteClasses({
	CuteSuiteWizardHandlerTest.class,
	SourceActionsTests.class,
	UnregisteredTestFunctionCheckerTest.class,
	})
//@formatter:off
public class PluginUITestSuiteAll { }
