/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
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
	public void addTestFiles(IFolder folder, IProgressMonitor monitor) throws CoreException {
		suitename=suitewizPage.getSuiteName();
		copyFiles(folder,monitor);
	}
	//extract method for unit testing
	public void copyFiles(IFolder folder, IProgressMonitor monitor) throws CoreException {
		copyFile(folder,monitor,"Test.cpp","Test.cpp");
		copyFile(folder,monitor,"$suitename$.cpp",suitename+".cpp");
		copyFile(folder,monitor,"$suitename$.h",suitename+".h");
	}
	@SuppressWarnings("unchecked")
	protected void copyFile(IFolder folder, IProgressMonitor monitor,String templateFilename, String targetFilename)throws CoreException{
		Enumeration en = CuteLauncherPlugin.getDefault().getBundle().findEntries("templates/projecttemplates/suite", templateFilename, false);
		if(en.hasMoreElements()){
			URL url = (URL)en.nextElement();
			IFile targetFile = folder.getFile(targetFilename);
			try {				
				ByteArrayInputStream str=implantActualsuitename(url);
				
				targetFile.create(str,IResource.FORCE , new SubProgressMonitor(monitor,1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,CuteLauncherPlugin.PLUGIN_ID,42,e.getMessage(), e));
			}
		}else{
			throw new CoreException(new Status(IStatus.ERROR,CuteLauncherPlugin.PLUGIN_ID,42,"missing suite template files", null));
		}
	}
	
	//parse the template source file for $suitename$ and replace it with the user's entry
	public ByteArrayInputStream implantActualsuitename(URL url)throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder buffer = new StringBuilder();
		String linesep=System.getProperty("line.separator");
		while(br.ready()){
			String a=br.readLine();
			buffer.append(a.replaceAll("[$]suitename[$]", suitename)+linesep);
		}
		return new ByteArrayInputStream(buffer.toString().getBytes());
	}
}
