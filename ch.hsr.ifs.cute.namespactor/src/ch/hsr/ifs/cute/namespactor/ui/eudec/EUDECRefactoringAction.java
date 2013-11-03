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
package ch.hsr.ifs.cute.namespactor.ui.eudec;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

/**
 * @author Jules Weder
 * */
public class EUDECRefactoringAction extends RefactoringAction {

	public EUDECRefactoringAction(String label) {
		super(label);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (!hasFileResource(wc)) {
			return;
		}
		EUDECRefactoringRunner runner = new EUDECRefactoringRunner(wc, selection, shellProvider, wc.getCProject());
		runner.run();

	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {

	}

	private boolean hasFileResource(IWorkingCopy wc) {
		return (wc.getResource() instanceof IFile);
	}

}
