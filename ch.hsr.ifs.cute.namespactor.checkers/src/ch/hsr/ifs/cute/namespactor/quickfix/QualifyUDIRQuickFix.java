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
package ch.hsr.ifs.cute.namespactor.quickfix;

import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;

import ch.hsr.ifs.cute.namespactor.ui.iu.IURefactoringActionDelegate;
import ch.hsr.ifs.cute.namespactor.ui.qun.QUNRefactoringAction;

/**
 * @author kunz@ideadapt.net
 * */
public class QualifyUDIRQuickFix extends RefactoringMarkerResolution {

	public static final String LABEL = "ch.hsr.ifs.cute.namespactor.qun";

	@Override
	public String getLabel() {
		return "Qualify name";
	}

	@Override
	protected RefactoringAction getRefactoringAction() {
		return new QUNRefactoringAction(IURefactoringActionDelegate.ACTION_ID);
	}
}