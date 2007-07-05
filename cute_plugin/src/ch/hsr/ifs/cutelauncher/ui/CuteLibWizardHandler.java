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
package ch.hsr.ifs.cutelauncher.ui;

import java.util.Vector;

import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Emanuel Graf
 *
 */
public class CuteLibWizardHandler extends CuteWizardHandler {
	
	
	private LibReferencePage libRefPage;
	public CuteLibWizardHandler(Composite p, IWizard w) {
		
		super( p, w);
		libRefPage = new LibReferencePage(getConfigPage(), getStartingPage());
		libRefPage.setPreviousPage(getStartingPage());
		libRefPage.setWizard(getWizard());
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(libRefPage, libRefPage.getPageID());
	}

	@Override
	public void createProject(IProject project, boolean defaults)
			throws CoreException {
		super.createProject(project, defaults);
		createLibSetings(project);
	}
	
	@Override
	public void createProject(IProject project, boolean defaults,
			boolean onFinish) throws CoreException {
		super.createProject(project, defaults, onFinish);
		createLibSetings(project);
	}

	private void createLibSetings(IProject project) throws CoreException {
		Vector<IProject> projects = libRefPage.getCheckedProjects();
		for (IProject libProject : projects) {
			setLibReferences(project, libProject);
		}
	}
	
	



	private void setLibReferences(IProject project, IProject libProject) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
		IConfiguration config = info.getDefaultConfiguration();
		ICSourceEntry[] sources = config.getSourceEntries();
		for (ICSourceEntry sourceEntry : sources) {
			IPath location = sourceEntry.getFullPath();
			if(location.segmentCount()== 0) {
				setIncludePaths(libProject.getFullPath(), project);
			}else {
				setIncludePaths(libProject.getFolder(location).getFullPath(), project);
			}
		}
		ICOutputEntry[]  dirs = config.getBuildData().getOutputDirectories();
		for (ICOutputEntry outputEntry : dirs) {
			IPath location = outputEntry.getFullPath();
			setLibraryPaths(libProject.getFolder(location.removeFirstSegments(1)), project);
			setLibName(config.getArtifactName(), project);
		}
		
		setProjectReference(project, libProject);
		
	}



	private void setProjectReference(IProject project, IProject libProject) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setReferencedProjects(new IProject[] {libProject});
		project.setDescription(desc, IResource.KEEP_HISTORY, new NullProgressMonitor());
	}

	@Override
	public IWizardPage getSpecificPage() {
		
		return libRefPage;
	}

	protected void setLibraryPaths(IFolder libFolder, IProject project)
			throws CoreException {
				String path = "\"${workspace_loc:" + libFolder.getFullPath().toPortableString() + "}\"";
				setOptionInAllConfigs(project, path, IOption.LIBRARY_PATHS);
			}
	
	
	
	protected void setLibName(String libName, IProject project) throws CoreException {
		setOptionInAllConfigs(project, libName, IOption.LIBRARIES);
	}

	

	@Override
	protected GetOptionsStrategy getStrategy(int optionType) {
		switch (optionType) {
		case IOption.LIBRARY_PATHS:
			return new LibraryPathsStrategy();
		
		case IOption.LIBRARIES:
			return new LibrariesStrategy();

		default:
			return super.getStrategy(optionType);
		}
		
	}



	private class LibraryPathsStrategy implements GetOptionsStrategy{

		public String[] getValues(IOption option) throws BuildException {
			return option.getBasicStringListValue();
		}
		
	}
	
	private class LibrariesStrategy implements GetOptionsStrategy{

		public String[] getValues(IOption option) throws BuildException {
			return option.getLibraries();
		}
		
	}
}
