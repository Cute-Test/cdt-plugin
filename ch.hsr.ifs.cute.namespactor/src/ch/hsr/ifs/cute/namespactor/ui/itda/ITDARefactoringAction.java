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
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

public class ITDARefactoringAction extends RefactoringAction {

	public ITDARefactoringAction(String label) {
		super(label);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (!hasFileResource(wc)) {
			return;
		}
		ITDARefactoringRunner runner = new ITDARefactoringRunner(wc, selection, shellProvider, wc.getCProject());
		runner.run();
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	private boolean hasFileResource(IWorkingCopy wc) {
		return (wc.getResource() instanceof IFile);
	}

}
