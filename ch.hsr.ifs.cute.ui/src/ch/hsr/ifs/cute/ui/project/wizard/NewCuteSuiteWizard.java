/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.core.CuteCorePlugin;

public class NewCuteSuiteWizard extends NewCuteProjectCategoryWizard {
	/*
	 * left hand side tree
	 * 
	 * @see ch.hsr.ifs.cutelauncher.ui.NewCuteProjectWizard#getEntryDescriptor(ch.hsr.ifs.cutelauncher.ui.CuteWizardHandler)
	 */
	@Override
	protected EntryDescriptor getEntryDescriptor(CuteWizardHandler handler) {
		Image proImg = CuteCorePlugin.getImageDescriptor("obj16/cute_app.png").createImage(); //$NON-NLS-1$
		return new EntryDescriptor("ch.hsr.ifs.cutelauncher.SuiteProjectType", ID, Messages.getString("NewCuteSuiteWizard.CuteSuiteModule"), false, handler, proImg); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * The actual execution of the wizard
	 * 
	 * @see ch.hsr.ifs.cutelauncher.ui.NewCuteProjectWizard#getHandler(org.eclipse.jface.wizard.IWizard)
	 */
	@Override
	protected CuteWizardHandler getHandler(IWizard wizard) {
		return new CuteSuiteWizardHandler(parent, wizard);
	}
}