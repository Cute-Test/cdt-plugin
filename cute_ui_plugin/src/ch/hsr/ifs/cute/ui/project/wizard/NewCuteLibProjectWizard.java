/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
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
public class NewCuteLibProjectWizard extends NewCuteProjectWizard {

	@Override
	protected EntryDescriptor getEntryDescriptor(CuteWizardHandler handler) {
		Image proImg = CuteCorePlugin.getImageDescriptor("obj16/cute_app.png").createImage(); //$NON-NLS-1$
		return new EntryDescriptor("ch.hsr.ifs.cutelauncher.libProjectType", null, Messages.getString("NewCuteLibProjectWizard.CuteLibTestProject"), false, handler, proImg); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected CuteWizardHandler getHandler(IWizard wizard) {
		return new CuteLibWizardHandler(parent, wizard);
	}

}
