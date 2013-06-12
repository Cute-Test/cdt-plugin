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
package ch.hsr.ifs.cdt.namespactor.rtstest.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

import ch.hsr.ifs.cdt.namespactor.refactoring.qun.QUNRefactoring;
import ch.hsr.ifs.cdt.namespactor.rtstest.testinfrastructure.JUnit4RtsRefactoringTest;

@SuppressWarnings("restriction")
public class QUNRefactoringTest extends JUnit4RtsRefactoringTest {
	
	@Override
	protected CRefactoring getRefactoring() throws CModelException {
		
		return new QUNRefactoring(getCElementOfTestFile(), selection, cproject);
	}

}
