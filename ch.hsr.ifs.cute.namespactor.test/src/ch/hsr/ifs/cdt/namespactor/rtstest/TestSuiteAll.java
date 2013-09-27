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
package ch.hsr.ifs.cdt.namespactor.rtstest;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.cdt.namespactor.rtstest.tests.EUDECRefactoringTest;
import ch.hsr.ifs.cdt.namespactor.rtstest.tests.EUDIRRefactoringTest;
import ch.hsr.ifs.cdt.namespactor.rtstest.tests.IUDECRefactoringTest;
import ch.hsr.ifs.cdt.namespactor.rtstest.tests.IUDIRRefactoringTest;
import ch.hsr.ifs.cdt.namespactor.rtstest.tests.IURefactoringTestIUDEC;
import ch.hsr.ifs.cdt.namespactor.rtstest.tests.IURefactoringTestIUDIR;
import ch.hsr.ifs.cdt.namespactor.rtstest.tests.QUNRefactoringTest;


@RunWith(Suite.class)
@SuiteClasses({
			//@formatter:off
	IURefactoringTestIUDEC.class,
	IURefactoringTestIUDIR.class,
	IUDIRRefactoringTest.class,
	IUDECRefactoringTest.class,
	QUNRefactoringTest.class,
	EUDIRRefactoringTest.class,
	EUDECRefactoringTest.class
			//@formatter:on
})
public class TestSuiteAll {
}
