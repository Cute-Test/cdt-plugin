/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.tdd.linkedMode.ChangeRecorderTest;
import ch.hsr.ifs.cute.tdd.linkedMode.CreateClassLMTest;
import ch.hsr.ifs.cute.tdd.linkedMode.NestedEditTest;
import ch.hsr.ifs.cute.tdd.linkedMode.TddLinkedModeTest;
import ch.hsr.ifs.cute.tdd.ui.tests.AllTddTests;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
    AllTddTests.class,
    ChangeRecorderTest.class,
    CreateClassLMTest.class,
    NestedEditTest.class,
    TddLinkedModeTest.class,
//@formatter:on
})
public class TestSuiteAll {
}
