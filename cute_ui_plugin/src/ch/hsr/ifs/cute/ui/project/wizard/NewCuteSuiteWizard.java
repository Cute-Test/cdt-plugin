/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.core.CuteCorePlugin;

public class NewCuteSuiteWizard extends NewCuteProjectWizard {
	/*
	 * left hand side tree
	 * @see ch.hsr.ifs.cutelauncher.ui.NewCuteProjectWizard#getEntryDescriptor(ch.hsr.ifs.cutelauncher.ui.CuteWizardHandler)
	 */
	@Override
	protected EntryDescriptor getEntryDescriptor(CuteWizardHandler handler) {
		Image proImg = CuteCorePlugin.getImageDescriptor("obj16/cute_app.png").createImage(); //$NON-NLS-1$
		return new EntryDescriptor("ch.hsr.ifs.cutelauncher.SuiteProjectType", null, Messages.getString("NewCuteSuiteWizard.CuteSuiteModule"), false, handler, proImg); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * The actual execution of the wizard
	 * @see ch.hsr.ifs.cutelauncher.ui.NewCuteProjectWizard#getHandler(org.eclipse.jface.wizard.IWizard)
	 */
	@Override
	protected CuteWizardHandler getHandler(IWizard wizard) {
		return new CuteSuiteWizardHandler(parent, wizard);
	}
}
