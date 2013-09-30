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
package ch.hsr.ifs.cute.namespactor.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class NSRefactoringWizard extends RefactoringWizard {

	public NSRefactoringWizard(Refactoring refactoring, int flags) {
		super(refactoring, flags);
	}

	public NSRefactoringWizard(Refactoring astCache) {
		this(astCache, WIZARD_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
	}

}
