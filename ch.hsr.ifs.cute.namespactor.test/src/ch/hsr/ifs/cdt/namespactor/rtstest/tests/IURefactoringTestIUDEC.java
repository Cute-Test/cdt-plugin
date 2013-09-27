/******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Peter Sommerlad, IFS
 ******************************************************************************/
package ch.hsr.ifs.cdt.namespactor.rtstest.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

import ch.hsr.ifs.cdt.namespactor.refactoring.iu.IURefactoring;
import ch.hsr.ifs.cdt.namespactor.rtstest.testinfrastructure.JUnit4RtsRefactoringTest;
import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;

@SuppressWarnings("restriction")
@RunFor(rtsFile = "/resources//tests/IUDECRefactoringTest.rts")
public class IURefactoringTestIUDEC extends JUnit4RtsRefactoringTest {

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
