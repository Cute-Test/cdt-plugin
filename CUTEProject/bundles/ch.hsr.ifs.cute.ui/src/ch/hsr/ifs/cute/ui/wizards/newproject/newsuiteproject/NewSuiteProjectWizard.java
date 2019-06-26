/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.newproject.newsuiteproject;

import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.wizards.newproject.NewProjectWizard;
import ch.hsr.ifs.cute.ui.wizards.newproject.NewProjectWizardHandler;


public class NewSuiteProjectWizard extends NewProjectWizard {

    @Override
    protected EntryDescriptor getEntryDescriptor(NewProjectWizardHandler handler) {
        Image proImg = CuteCorePlugin.getImageDescriptor("obj16/cute_app.png").createImage();
        String name = NewSuiteProjectMessages.Name;
        return new EntryDescriptor("ch.hsr.ifs.cutelauncher.SuiteProjectType", ID, name, false, handler, proImg);
    }

    @Override
    protected NewProjectWizardHandler getHandler(IWizard wizard) {
        return new NewSuiteProjectWizardHandler(parent, wizard);
    }
}
