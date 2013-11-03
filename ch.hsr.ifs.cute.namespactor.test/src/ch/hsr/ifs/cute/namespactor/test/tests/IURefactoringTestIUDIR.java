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
package ch.hsr.ifs.cute.namespactor.test.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.namespactor.refactoring.iu.IURefactoring;
import ch.hsr.ifs.cute.namespactor.test.testinfrastructure.JUnit4RtsRefactoringTest;

@SuppressWarnings("restriction")
@RunFor(rtsFile = "/resources//tests/IUDIRRefactoringTest.rts")
public class IURefactoringTestIUDIR extends JUnit4RtsRefactoringTest {

	@Override
	protected CRefactoring getRefactoring() throws CModelException {
		return new IURefactoring(getCElementOfTestFile(), selection, cproject);
	}

	@Override
	public void setUp() throws Exception {
		addIncludeDirPath("include");
		super.setUp();
	}

}
