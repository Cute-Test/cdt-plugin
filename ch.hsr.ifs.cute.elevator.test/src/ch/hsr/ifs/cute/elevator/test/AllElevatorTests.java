/******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik 
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

import ch.hsr.ifs.cute.elevator.test.checker.InitializerCheckerNegativeMatchesTest;
import ch.hsr.ifs.cute.elevator.test.checker.InitializerCheckerPositiveMatchesTest;
import ch.hsr.ifs.cute.elevator.test.quickfix.InitializerQuickFixTest;
import ch.hsr.ifs.cute.elevator.test.refactoring.ElevateProjectRefactoringTest;



@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	InitializerCheckerNegativeMatchesTest.class,
	InitializerCheckerPositiveMatchesTest.class,
	InitializerQuickFixTest.class, 
	ElevateProjectRefactoringTest.class,
//@formatter:on
})
public class AllElevatorTests {
}