/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests.quickFixes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.tdd.linkedMode.ChangeRecorderTest;
import ch.hsr.ifs.cute.tdd.linkedMode.CreateClassLMTest;
import ch.hsr.ifs.cute.tdd.linkedMode.NestedEditTest;
import ch.hsr.ifs.cute.tdd.linkedMode.TddLinkedModeTest;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
			AddArgumentTest.class,
			ChangeVisibilityTest.class,
			CreateClassTest.class,
			CreateConstructorTest.class,
			CreateFunctionTest.class,
			CreateLocalVariableTest.class,
			CreateMemberVariableAndPrivateLabelTest.class,
			CreateMemberVariableTest.class,
			CreateOperatorTest.class,
			RemoveArgumentTest.class,
			NestedEditTest.class,
			ChangeRecorderTest.class,
			TddLinkedModeTest.class,
			CreateClassLMTest.class
//@formatter:on
})
public class AllQuickFixTests {
}
