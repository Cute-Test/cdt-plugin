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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

public class CuteSuiteWizardHandler extends CuteWizardHandler {
	private final NewCuteSuiteWizardCustomPage suitewizPage;
	String suitename;
	
	//for unit testing
	public CuteSuiteWizardHandler(String suitename){
		this(null,null);
		this.suitename=suitename;
	}
	public CuteSuiteWizardHandler(Composite p, IWizard w) {
		super( p, w);
		suitewizPage = new NewCuteSuiteWizardCustomPage(getConfigPage(), getStartingPage(), this);
		suitewizPage.setPreviousPage(getStartingPage());
		suitewizPage.setWizard(getWizard());
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(suitewizPage, suitewizPage.getPageID());
	}
	@Override
	public IWizardPage getSpecificPage() {
		return suitewizPage;
	}
	
	@Override
	protected void createCuteProjectSettings(IProject newProject, IProgressMonitor pm) {
		try {
			createCuteProject(newProject, pm);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void copyFiles(IFolder srcFolder, ICuteHeaders cuteVersion,
			IFolder cuteFolder) throws CoreException {
		suitename=suitewizPage.getSuiteName();
		cuteVersion.copyHeaderFiles(cuteFolder, new NullProgressMonitor());
		cuteVersion.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
		
	}
	
	@Override
	protected ICuteHeaders getCuteVersion() {
		return UiPlugin.getCuteVersion(suitewizPage.getCuteVersion());
	}
	@Override
	public boolean canFinish() {
		return suitewizPage.isCustomPageComplete();
	}
	
}