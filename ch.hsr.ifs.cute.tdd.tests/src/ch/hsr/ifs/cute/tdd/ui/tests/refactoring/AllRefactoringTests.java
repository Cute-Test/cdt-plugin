/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests.refactoring;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
			AddArgumentRefactoringTest.class,
			ChangeVisibilityRefactoringTest.class,
			CreateTypeRefactoringTest.class,
			CreateConstructorRefactoringTest.class,
			CreateFreeOperatorRefactoringTest.class,
			CreateFunctionParameterRefactoringTest.class,
			CreateFunctionRefactoringTest.class,
			CreateLocalVariableRefactoringTest.class,
			CreateMemberFunctionRefactoringTest.class,
			CreateMemberVariableRefactoringTest.class,
			CreateNamespaceRefactoringTest.class,
			CreateOperatorRefactoringTest.class,
			CreateStaticFunctionRefactoringTest.class,
			ExtractFunctionRefactoringTest.class,
			ExtractTypeRefactoringTest.class
			//@formatter:on
})
public class AllRefactoringTests {
}
