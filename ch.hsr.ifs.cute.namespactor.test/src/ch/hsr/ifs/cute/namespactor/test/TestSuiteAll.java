/******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cute.namespactor.checker.tests.UsingCheckerTest;
import ch.hsr.ifs.cute.namespactor.test.tests.EUDecRefactoringTest;
import ch.hsr.ifs.cute.namespactor.test.tests.EUDirRefactoringTest;
import ch.hsr.ifs.cute.namespactor.test.tests.ITDARefactoringTest;
import ch.hsr.ifs.cute.namespactor.test.tests.IUDecRefactoringTest;
import ch.hsr.ifs.cute.namespactor.test.tests.IUDirRefactoringTest;
import ch.hsr.ifs.cute.namespactor.test.tests.QUNRefactoringTest;
import ch.hsr.ifs.cute.namespactor.test.tests.TD2ARefactoringTest;

@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
	ITDARefactoringTest.class,
	TD2ARefactoringTest.class,
	IUDirRefactoringTest.class,
	IUDecRefactoringTest.class,
	QUNRefactoringTest.class,
	EUDirRefactoringTest.class, 
	EUDecRefactoringTest.class,
	TestSuiteSandbox.class, 
	UsingCheckerTest.class,
//@formatter:on
})
public class TestSuiteAll {
}
