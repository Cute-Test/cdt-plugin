/*******************************************************************************
 * Copyright (c) 2007, 2010 Institute for Software, HSR Hochschule für Technik
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

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.cute.ui.GetOptionsStrategy;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.IIncludeStrategyProvider;
import ch.hsr.ifs.cute.ui.IncludePathStrategy;
import ch.hsr.ifs.cute.ui.ProjectTools;
import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.CuteNature;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author Emanuel Graf
 *
 */
public class CuteWizardHandler extends MBSWizardHandler implements IIncludeStrategyProvider {
	
	private CuteVersionWizardPage cuteVersionWizardPage;

	@Override
	public IWizardPage getSpecificPage() {
		return cuteVersionWizardPage;
	}

	public CuteWizardHandler(Composite p, IWizard w) {
		super(new CuteBuildPropertyValue(), p, w);
		cuteVersionWizardPage = new CuteVersionWizardPage( getConfigPage(), getStartingPage(), this);
		cuteVersionWizardPage.setWizard(w);
		
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(cuteVersionWizardPage, cuteVersionWizardPage.getPageID());
	}
	
	@Override
	public boolean canFinish() {
		return cuteVersionWizardPage != null ? cuteVersionWizardPage.isCustomPageComplete() : false;
	}	

	@Override
	public void postProcess(IProject newProject, boolean created) {
		if (created) {
			doTemplatesPostProcess(newProject);
			doCustom(newProject);
		}
	}

	@Override
	protected void doCustom(IProject newProject) {
		super.doCustom(newProject);
		createCuteProjectSettings(newProject);
	}

	protected void createCuteProjectSettings(IProject newProject) {
		try {
			createCuteProject(newProject);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}


	protected void createCuteProject(IProject project) throws CoreException {
		CuteNature.addCuteNature(project, new NullProgressMonitor());
		QualifiedName key = new QualifiedName(UiPlugin.PLUGIN_ID, UiPlugin.CUTE_VERSION_PROPERTY_NAME);
		project.setPersistentProperty(key, getCuteVersion().getVersionString());
		createCuteProjectFolders(project);
		callAdditionalHandlers(project);
		ManagedBuildManager.saveBuildInfo(project, true);
		
	}

	private void callAdditionalHandlers(IProject project) throws CoreException {
		List<ICuteWizardAddition> adds = cuteVersionWizardPage.getAdditions();
		for (ICuteWizardAddition addition : adds) {
			addition.getHandler().configureProject(project);
		}
		
		
	}


	protected void createCuteProjectFolders(IProject project)
			throws CoreException {
		IFolder srcFolder = ProjectTools.createFolder(project, "src", true); //$NON-NLS-1$
		ICuteHeaders cuteVersion = getCuteVersion();
		
		
		IFolder cuteFolder = ProjectTools.createFolder(project, "cute", true); //$NON-NLS-1$
		
		
		copyFiles(srcFolder, cuteVersion, cuteFolder);
		
		ProjectTools.setIncludePaths(cuteFolder.getFullPath(), project, this);
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				getTestMainFile(project), true);
	}

	protected ICuteHeaders getCuteVersion() {
		return UiPlugin.getCuteVersion(cuteVersionWizardPage.getCuteVersionString());
	}

	public void copyFiles(IFolder srcFolder, ICuteHeaders cuteVersion,
			IFolder cuteFolder) throws CoreException {
		cuteVersion.copyTestFiles(srcFolder, new NullProgressMonitor());
		cuteVersion.copyHeaderFiles(cuteFolder, new NullProgressMonitor());
	}
	
	protected IFile getTestMainFile(IProject project) {
		return project.getFile("src/Test.cpp"); //$NON-NLS-1$
	}
	
	public GetOptionsStrategy getStrategy(int optionType) {
		switch (optionType) {
		case IOption.INCLUDE_PATH:
			return new IncludePathStrategy();

		default:
			throw new IllegalArgumentException("Illegal Argument: "+optionType); //$NON-NLS-1$
		}
	}


}
