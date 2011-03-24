/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.core.CuteCorePlugin;

/**
 * @author Emanuel Graf
 *
 */
public class NewCuteProjectWizard extends AbstractCWizard {

	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		CuteWizardHandler handler = getHandler(wizard);
		IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MBSWizardHandler.ARTIFACT, new CuteBuildPropertyValue().getId());
		for (int j=0; j<tcs.length; j++) {
			if (!supportedOnly || isValid(tcs[j], true, wizard)) {
				handler.addTc(tcs[j]);
			}
		}
		EntryDescriptor data = getEntryDescriptor(handler);
		return new EntryDescriptor[] {data};
	}

	protected EntryDescriptor getEntryDescriptor(CuteWizardHandler handler) {
		Image proImg = CuteCorePlugin.getImageDescriptor("obj16/cute_app.png").createImage(); //$NON-NLS-1$
		return new EntryDescriptor("ch.hsr.ifs.cutelauncher.projectType", null, Messages.getString("NewCuteProjectWizard.CuteProject"), false, handler, proImg); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected CuteWizardHandler getHandler(IWizard wizard) {
		return new CuteWizardHandler(parent, wizard);
	}

}
