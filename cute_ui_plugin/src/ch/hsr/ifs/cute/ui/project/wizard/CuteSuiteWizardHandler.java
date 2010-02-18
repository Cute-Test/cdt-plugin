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

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

public class CuteSuiteWizardHandler extends CuteWizardHandler {
	private final NewCuteSuiteWizardCustomPage suitewizPage;
	
	//for unit testing
	public CuteSuiteWizardHandler(String suitename){
		this(null,null);
		this.suitename=suitename;
	}
	public CuteSuiteWizardHandler(Composite p, IWizard w) {
		super( p, w);
		suitewizPage = new NewCuteSuiteWizardCustomPage(getConfigPage(), getStartingPage());
		suitewizPage.setPreviousPage(getStartingPage());
		suitewizPage.setWizard(getWizard());
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(suitewizPage, suitewizPage.getPageID());
	}
	@Override
	public IWizardPage getSpecificPage() {
		return suitewizPage;
	}

	String suitename;

	
	@Override
	public void copyFiles(IFolder srcFolder, ICuteHeaders cuteVersion,
			IFolder cuteFolder) throws CoreException {
		suitename=suitewizPage.getSuiteName();
		cuteVersion.copyHeaderFiles(cuteFolder, new NullProgressMonitor());
		cuteVersion.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
		
	}
	
	@Override
	protected ICuteHeaders getCuteVersion() {
		return getCuteVersion(suitewizPage.getCuteVersion());
	}
	@Override
	public boolean canFinish() {
		return suitewizPage.isCustomPageComplete();
	}

	
}
