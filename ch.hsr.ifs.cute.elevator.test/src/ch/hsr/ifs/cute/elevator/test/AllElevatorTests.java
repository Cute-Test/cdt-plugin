/******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Thomas Corbat - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.elevator.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.elevator.test.checker.DefaultConstructorNegativeMatchesTest;
import ch.hsr.ifs.cute.elevator.test.checker.DefaultConstructorPositiveMatchesTest;
import ch.hsr.ifs.cute.elevator.test.checker.InitializationCheckerNegativeMatchesTest;
import ch.hsr.ifs.cute.elevator.test.checker.InitializationCheckerPositiveMatchesTest;
import ch.hsr.ifs.cute.elevator.test.checker.NullMacroCheckerTest;
import ch.hsr.ifs.cute.elevator.test.quickfix.InitializationQuickFixTest;
import ch.hsr.ifs.cute.elevator.test.quickfix.ReplaceNullMacroQuickFixTest;
import ch.hsr.ifs.cute.elevator.test.refactoring.ElevateProjectRefactoringTest;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	InitializationCheckerNegativeMatchesTest.class,
	InitializationCheckerPositiveMatchesTest.class,
	InitializationQuickFixTest.class, 
	ElevateProjectRefactoringTest.class,
	DefaultConstructorNegativeMatchesTest.class,
	DefaultConstructorPositiveMatchesTest.class,
	NullMacroCheckerTest.class,
	ReplaceNullMacroQuickFixTest.class
//@formatter:on
})
public class AllElevatorTests {
}
