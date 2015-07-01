/******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik 
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors:
 * 	Peter Sommerlad <peter.sommerlad@hsr.ch>
 ******************************************************************************/
package ch.hsr.ifs.cute.namespactor.ui.itda;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import ch.hsr.ifs.cute.namespactor.refactoring.NSRefactoringWizard;
import ch.hsr.ifs.cute.namespactor.refactoring.itda.ITDARefactoring;

@SuppressWarnings("restriction")
public class ITDARefactoringRunner extends RefactoringRunner {

	public ITDARefactoringRunner(ICElement element, ISelection selection, IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run() {
		CRefactoring refactoring = new ITDARefactoring(element, selection, project);
		RefactoringWizard wizard = new NSRefactoringWizard(refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
	}

}
