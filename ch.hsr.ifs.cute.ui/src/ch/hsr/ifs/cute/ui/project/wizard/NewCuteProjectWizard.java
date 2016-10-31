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

/**
 * @author Emanuel Graf
 * 
 */
public class NewCuteProjectWizard extends NewCuteProjectCategoryWizard {

	@Override
	protected EntryDescriptor getEntryDescriptor(CuteWizardHandler handler) {
		Image proImg = CuteCorePlugin.getImageDescriptor("obj16/cute_app.png").createImage();
		String name = Messages.getString("NewCuteProjectWizard.CuteProject");
		return new EntryDescriptor("ch.hsr.ifs.cutelauncher.projectType", ID, name, false, handler, proImg);
	}

	@Override
	protected CuteWizardHandler getHandler(IWizard wizard) {
		return new CuteWizardHandler(parent, wizard);
	}
	
}
