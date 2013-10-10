/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

public class CuteSuiteWizardHandler extends CuteWizardHandler {
	private NewCuteSuiteWizardCustomPage suitewizPage;
	String suitename;

	public CuteSuiteWizardHandler(String suitename) {
		this(null, null);
		this.suitename = suitename;
	}

	public CuteSuiteWizardHandler(Composite p, IWizard w) {
		super(p, w);
		suitewizPage.setPreviousPage(getStartingPage());
		suitewizPage.setWizard(getWizard());
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(suitewizPage, suitewizPage.getPageID());
	}

	@Override
	protected CuteVersionWizardPage initPage() {
		suitewizPage = new NewCuteSuiteWizardCustomPage(getConfigPage(), getStartingPage());
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
