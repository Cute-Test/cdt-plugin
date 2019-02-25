/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.newproject.newsuiteproject;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.wizards.newproject.NewProjectWizardHandler;
import ch.hsr.ifs.cute.ui.wizards.newproject.NewProjectWizardPage;


public class NewSuiteProjectWizardHandler extends NewProjectWizardHandler {

    private NewSuiteProjectWizardPage suitewizPage;
    String                                suitename;

    public NewSuiteProjectWizardHandler(String suitename) {
        this(null, null);
        this.suitename = suitename;
    }

    public NewSuiteProjectWizardHandler(Composite p, IWizard w) {
        super(p, w);
        suitewizPage.setPreviousPage(getNewProjectCreationPage());
        suitewizPage.setWizard(getWizard());
        MBSCustomPageManager.init();
        MBSCustomPageManager.addStockPage(suitewizPage, suitewizPage.getPageID());
    }

    @Override
    protected NewProjectWizardPage initPage() {
        suitewizPage = new NewSuiteProjectWizardPage(getConfigPage(), getNewProjectCreationPage(), getWizardContainer(getWizard()));
        return suitewizPage;
    }

    @Override
    public IWizardPage getSpecificPage() {
        return suitewizPage;
    }

    @Override
    public void copyExampleTestFiles(IFolder srcFolder, ICuteHeaders cuteVersion) throws CoreException {
        suitename = suitewizPage.getSuiteName();
        cuteVersion.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
    }

    @Override
    public boolean canFinish() {
        return suitewizPage.isCustomPageComplete();
    }
}
