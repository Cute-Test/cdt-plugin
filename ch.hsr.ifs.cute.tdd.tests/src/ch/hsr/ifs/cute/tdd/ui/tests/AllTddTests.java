/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.tdd.ui.tests.quickFixes.AllQuickFixTests;
import ch.hsr.ifs.cute.tdd.ui.tests.refactoring.AllRefactoringTests;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	AllRefactoringTests.class,
	AllQuickFixTests.class
//@formatter:on
})
public class AllTddTests {
}