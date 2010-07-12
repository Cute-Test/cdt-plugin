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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;


/**
 * @author Emanuel Graf
 *
 */
public class CuteLibWizardHandler extends CuteWizardHandler {
	
	
	private final LibReferencePage libRefPage;
	public CuteLibWizardHandler(Composite p, IWizard w) {
		
		super( p, w);
		libRefPage = new LibReferencePage(getConfigPage(), getStartingPage(),w.getContainer(), this);
		libRefPage.setPreviousPage(getStartingPage());
		libRefPage.setWizard(getWizard());
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(libRefPage, libRefPage.getPageID());
	}

	@Override
	protected void createCuteProjectSettings(IProject newProject) {
		try {
			createCuteProject(newProject);
			createLibSetings(newProject);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=229085
	@SuppressWarnings({ "unchecked", "rawtypes" })
	//@see org.eclipse.cdt.managedbuilder.core.tests/tests/org/eclipse/cdt/projectmodel/tests/ProjectModelTests.testReferences()
	protected void createCDTProjectReference(IProject project) throws CoreException {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des4 = coreModel.getProjectDescription(project);
		ICConfigurationDescription dess[] = des4.getConfigurations();
	
		Vector<IProject> projects = libRefPage.getCheckedProjects();

		Map prjRefs=null;
		for(int x=0;x<dess.length;x++){
			if(x==0){
				prjRefs= new HashMap();
				for(IProject p:projects){
					String prjname=p.getName();
					prjRefs.put(prjname, "");		 //$NON-NLS-1$
				}
			}
			dess[x].setReferenceInfo(prjRefs);
		}
		coreModel.setProjectDescription(project, des4);
		for(IProject p:projects){
			String prjname=p.getName();
			setLibName(prjname,project);	
		}
	}
	
	private void createLibSetings(IProject project) throws CoreException {
		Vector<IProject> projects = libRefPage.getCheckedProjects();
		for (IProject libProject : projects) {
			for (ICuteWizardAddition addition : libRefPage.getAdditions()) {
				addition.getHandler().configureLibProject(libProject);
			}
			setToolChainIncludePath(project, libProject);
		}
		setProjectReference(project, projects);
		ManagedBuildManager.saveBuildInfo(project, true);
	}

	

	private void setProjectReference(IProject project, Vector<IProject> projects)
			throws CoreException {
		if(projects.size()>0){
			IProjectDescription desc = project.getDescription();
			IProject iproject[]=new IProject[projects.size()];
			
			for(int x=0;x<projects.size();x++){
				iproject[x]=projects.elementAt(x);
			}
			
			desc.setReferencedProjects(iproject);
			project.setDescription(desc, IResource.KEEP_HISTORY, new NullProgressMonitor());
		}
	}
	


	private void setToolChainIncludePath(IProject project, IProject libProject) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
		IConfiguration config = info.getDefaultConfiguration();
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		ICSourceEntry[] sources = config.getSourceEntries();
		for (ICSourceEntry sourceEntry : sources) {
			IPath location = sourceEntry.getFullPath();
			if(location.segmentCount()== 0) {
				setIncludePaths(libProject.getFullPath(), project);
			}else {
				setIncludePaths(libProject.getFolder(location).getFullPath(), project);
			}
		}
		for(IConfiguration configuration : configs) {
			ICOutputEntry[]  dirs = configuration.getBuildData().getOutputDirectories();
			for (ICOutputEntry outputEntry : dirs) {
				IPath location = outputEntry.getFullPath();
				if(location.segmentCount()== 0){
					setLibraryPaths(libProject.getFullPath(), project, configuration);	
				}else{
					//IPath location1=location.removeFirstSegments(1);
					setLibraryPaths(libProject.getFolder(location).getFullPath(), project, configuration);	
				}
			}
		}
		String artifactName = config.getArtifactName();
		if(artifactName.equalsIgnoreCase("${ProjName}")) { //$NON-NLS-1$
			setLibName(libProject.getName(), project);
		}else{
			setLibName(artifactName, project);
		}
	}

	@Override
	public IWizardPage getSpecificPage() {
		
		return libRefPage;
	}

	protected void setLibraryPaths(IPath libFolder, IProject project, IConfiguration configuration)
			throws CoreException {
				String path = "\"${workspace_loc:" + libFolder.toPortableString() + "}\""; //$NON-NLS-1$ //$NON-NLS-2$
				IConfiguration targetConfig = findSameConfig(configuration, project);
				try {
					IToolChain toolChain = targetConfig.getToolChain();
					setOptionInConfig(path, targetConfig, toolChain.getOptions(), toolChain, IOption.LIBRARY_PATHS);
					ITool[] tools = targetConfig.getTools();
					for(int j=0; j<tools.length; j++) {
						setOptionInConfig(path, targetConfig, tools[j].getOptions(), tools[j], IOption.LIBRARY_PATHS);
					}
				} catch (BuildException be) {
					throw new CoreException(new Status(IStatus.ERROR,CuteCorePlugin.PLUGIN_ID,42,be.getMessage(), be));
				}
			}
	
	private IConfiguration findSameConfig(IConfiguration configuration, IResource project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		for (IConfiguration iConfiguration : configs) {
			if(iConfiguration.getName().equals(configuration.getName())) {
				return iConfiguration;
			}
		}
		return info.getDefaultConfiguration();
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
	
	

	@Override
	protected ICuteHeaders getCuteVersion() {
		return UiPlugin.getCuteVersion(libRefPage.getCuteVersionString());
	}

	//bugzilla #210116:on CDT spelling error
	@Override
	public boolean canFinish() {
		if(libRefPage ==null)return false;
		Vector<IProject> projects = libRefPage.getCheckedProjects();
		if(projects.size()<1)return false;
		return libRefPage.isCustomPageComplete();
	}

	private static class LibraryPathsStrategy implements GetOptionsStrategy{

		public String[] getValues(IOption option) throws BuildException {
			return option.getBasicStringListValue();
		}
		
	}
	
	private static class LibrariesStrategy implements GetOptionsStrategy{

		public String[] getValues(IOption option) throws BuildException {
			return option.getLibraries();
		}
		
	}
}
//to convert IFolder to IPath use (IResource)IFolder.getFullPath()
