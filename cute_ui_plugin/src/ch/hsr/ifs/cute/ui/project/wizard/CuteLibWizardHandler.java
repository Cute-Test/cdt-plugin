/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.GetOptionsStrategy;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.ProjectTools;
import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author Emanuel Graf
 * 
 */
public class CuteLibWizardHandler extends CuteWizardHandler {

	private final LibReferencePage libRefPage;

	public CuteLibWizardHandler(Composite p, IWizard w) {

		super(p, w);
		libRefPage = new LibReferencePage(getConfigPage(), getStartingPage(), getWizardContainer(w), this);
		libRefPage.setPreviousPage(getStartingPage());
		libRefPage.setWizard(getWizard());
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(libRefPage, libRefPage.getPageID());
	}

	private IWizardContainer getWizardContainer(IWizard w) {
		return w == null ? null : w.getContainer();
	}

	@Override
	protected void createCuteProjectSettings(IProject newProject, IProgressMonitor pm) {
		try {
			createCuteProject(newProject, pm);
			createLibSetings(newProject);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void createLibSetings(IProject project) throws CoreException {
		Vector<IProject> projects = libRefPage.getCheckedProjects();
		for (IProject libProject : projects) {
			for (ICuteWizardAddition addition : getAdditions()) {
				addition.getHandler().configureLibProject(libProject);
			}
			setToolChainIncludePath(project, libProject);
		}
		setProjectReference(project, projects);
		ManagedBuildManager.saveBuildInfo(project, true);
	}

	@Override
	protected List<ICuteWizardAddition> getAdditions() {
		return libRefPage.getAdditions();
	}

	private void setProjectReference(IProject project, Vector<IProject> projects) throws CoreException {
		if (projects.size() > 0) {
			ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project.getProject(), true);
			ICConfigurationDescription cfgs[] = des.getConfigurations();
			for (ICConfigurationDescription config : cfgs) {
				Map<String, String> refMap = config.getReferenceInfo();
				for (IProject refProject : projects) {
					refMap.put(refProject.getName(), ""); //$NON-NLS-1$
				}
				config.setReferenceInfo(refMap);
			}
			CCorePlugin.getDefault().setProjectDescription(project, des);
		}
	}

	private void setToolChainIncludePath(IProject project, IProject libProject) throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
		IConfiguration config = info.getDefaultConfiguration();
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		ICSourceEntry[] sources = config.getSourceEntries();
		for (ICSourceEntry sourceEntry : sources) {
			IPath location = sourceEntry.getFullPath();
			if (location.segmentCount() == 0) {
				ProjectTools.setIncludePaths(libProject.getFullPath(), project, this);
			} else {
				ProjectTools.setIncludePaths(libProject.getFolder(location).getFullPath(), project, this);
			}
		}
		for (IConfiguration configuration : configs) {
			ICOutputEntry[] dirs = configuration.getBuildData().getOutputDirectories();
			for (ICOutputEntry outputEntry : dirs) {
				IPath location = outputEntry.getFullPath();
				if (location.segmentCount() == 0) {
					setLibraryPaths(libProject.getFullPath(), project, configuration);
				} else {
					//IPath location1=location.removeFirstSegments(1);
					setLibraryPaths(libProject.getFolder(location).getFullPath(), project, configuration);
				}
			}
		}
		String artifactName = config.getArtifactName();
		if (artifactName.equalsIgnoreCase("${ProjName}")) { //$NON-NLS-1$
			setLibName(libProject.getName(), project);
		} else {
			setLibName(artifactName, project);
		}
	}

	@Override
	public IWizardPage getSpecificPage() {

		return libRefPage;
	}

	/**
	 * @since 4.0
	 */
	protected void setLibraryPaths(IPath libFolder, IProject project, IConfiguration configuration) throws CoreException {
		String path = "\"${workspace_loc:" + libFolder.toPortableString() + "}\""; //$NON-NLS-1$ //$NON-NLS-2$
		IConfiguration targetConfig = findSameConfig(configuration, project);
		try {
			IToolChain toolChain = targetConfig.getToolChain();
			ProjectTools.setOptionInConfig(path, targetConfig, toolChain.getOptions(), toolChain, IOption.LIBRARY_PATHS, this);
			ITool[] tools = targetConfig.getTools();
			for (int j = 0; j < tools.length; j++) {
				ProjectTools.setOptionInConfig(path, targetConfig, tools[j].getOptions(), tools[j], IOption.LIBRARY_PATHS, this);
			}
		} catch (BuildException be) {
			throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, be.getMessage(), be));
		}
	}

	private IConfiguration findSameConfig(IConfiguration configuration, IResource project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		for (IConfiguration iConfiguration : configs) {
			if (iConfiguration.getName().equals(configuration.getName())) {
				return iConfiguration;
			}
		}
		return info.getDefaultConfiguration();
	}

	protected void setLibName(String libName, IProject project) throws CoreException {
		ProjectTools.setOptionInAllConfigs(project, libName, IOption.LIBRARIES, this);
	}

	@Override
	public GetOptionsStrategy getStrategy(int optionType) {
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
		if (libRefPage == null)
			return false;
		Vector<IProject> projects = libRefPage.getCheckedProjects();
		if (projects.size() < 1)
			return false;
		return libRefPage.isCustomPageComplete();
	}

	private static class LibraryPathsStrategy implements GetOptionsStrategy {

		public String[] getValues(IOption option) throws BuildException {
			return option.getBasicStringListValue();
		}

	}

	private static class LibrariesStrategy implements GetOptionsStrategy {

		public String[] getValues(IOption option) throws BuildException {
			return option.getLibraries();
		}

	}
}
//to convert IFolder to IPath use (IResource)IFolder.getFullPath()
