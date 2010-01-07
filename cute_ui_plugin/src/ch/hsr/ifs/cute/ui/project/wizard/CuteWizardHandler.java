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
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ginkgo.gcov.builder.SampleNature;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.CuteNature;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author Emanuel Graf
 *
 */
public class CuteWizardHandler extends MBSWizardHandler {
	
	private static final String GCOV_LINKER_FLAGS = "-fprofile-arcs -ftest-coverage -std=c99";
	private static final String GNU_CPP_LINK_OPTION_FLAGS = "gnu.cpp.link.option.flags";
	private static final String GNU_CPP_LINKER_ID = "cdt.managedbuild.tool.gnu.cpp.linker";
	private static final String GNU_C_COMPILER_OPTION_MISC_OTHER = "gnu.c.compiler.option.misc.other";
	private static final String GNU_C_COMPILER_ID = "cdt.managedbuild.tool.gnu.c.compiler";
	private static final String GNU_CPP_COMPILER_OPTION_OTHER_OTHER = "gnu.cpp.compiler.option.other.other";
	private static final String GNU_CPP_COMPILER_ID = "cdt.managedbuild.tool.gnu.cpp.compiler";
	private static final String GCOV_COMPILER_FLAGS = "-fprofile-arcs -ftest-coverage -std=c99 ";
	private CuteVersionWizardPage cuteVersionWizardPage;

	@Override
	public IWizardPage getSpecificPage() {
		return cuteVersionWizardPage;
	}

	public CuteWizardHandler(Composite p, IWizard w) {
		super(new CuteBuildPropertyValue(), p, w);
		cuteVersionWizardPage = new CuteVersionWizardPage( getConfigPage(), getStartingPage());
		cuteVersionWizardPage.setWizard(w);
		
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(cuteVersionWizardPage, cuteVersionWizardPage.getPageID());
	}
	
	@Override
	public boolean canFinish() {
		return cuteVersionWizardPage != null ? cuteVersionWizardPage.isCustomPageComplete() : false;
	}

	@Override
	public void createProject(IProject project, boolean defaults,
			boolean onFinish) throws CoreException {
		super.createProject(project, defaults, onFinish);
		createCuteProject(project);
	}

	@Override
	public void createProject(IProject project, boolean defaults)
			throws CoreException {
		super.createProject(project, defaults);
		createCuteProject(project);
	}
	
	

	@Override
	public void createProject(IProject proj, boolean defaults, IProgressMonitor monitor) throws CoreException {
		super.createProject(proj, defaults, monitor);
		createCuteProject(proj);
	}
	
	public void createProject(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor) throws CoreException{
		super.createProject(project, defaults, onFinish, monitor);
		createCuteProject(project);
	}

	private void createCuteProject(IProject project) throws CoreException {
		CuteNature.addCuteNature(project, new NullProgressMonitor());
		createCuteProjectFolders(project);
		if(cuteVersionWizardPage.enableGcov) {
			configGcov(project);
		}
	}
	
	private void configGcov(IProject project) throws CoreException {
		setGcovNature(project);
		try {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			if(config.getParent().getId().equals("cdt.managedbuild.config.gnu.exe.debug")) {
				setOptionInTool(config, GNU_CPP_COMPILER_ID, GNU_CPP_COMPILER_OPTION_OTHER_OTHER, GCOV_COMPILER_FLAGS);
				setOptionInTool(config, GNU_C_COMPILER_ID, GNU_C_COMPILER_OPTION_MISC_OTHER, GCOV_COMPILER_FLAGS);
				setOptionInTool(config, GNU_CPP_LINKER_ID, GNU_CPP_LINK_OPTION_FLAGS, GCOV_LINKER_FLAGS);
				
			}
		}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR,UiPlugin.PLUGIN_ID,e.getMessage(),e));
		}
		
	}

	private void setOptionInTool(IConfiguration config, String toolId, String optionId, String optionValue)
			throws BuildException {
		ITool[] tools = config.getToolsBySuperClassId(toolId);
		for (ITool tool : tools) {
			IOption option = tool.getOptionById(optionId);
			option.setValue(option.getDefaultValue() == null ? optionValue : option.getDefaultValue().toString().trim() + " " + optionValue);
		}
	}

	private void setGcovNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
//		has nature?
		for (int i = 0; i < natures.length; i++) {
			if(SampleNature.NATURE_ID.equals(natures[i])){
				return;
			}
		}
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = SampleNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	protected void createCuteProjectFolders(IProject project)
			throws CoreException {
		IFolder srcFolder = createFolder(project, "src"); //$NON-NLS-1$
		ICuteHeaders cuteVersion = getCuteVersion();
		
		
		IFolder cuteFolder = createFolder(project, "cute"); //$NON-NLS-1$
		
		
		copyFiles(srcFolder, cuteVersion, cuteFolder);
		
		setIncludePaths(cuteFolder.getFullPath(), project);
		ManagedBuildManager.saveBuildInfo(project, true);
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				getTestMainFile(project), true);
	}

	protected ICuteHeaders getCuteVersion() {
		return getCuteVersion(cuteVersionWizardPage.getCuteVersionString());
	}

	public void copyFiles(IFolder srcFolder, ICuteHeaders cuteVersion,
			IFolder cuteFolder) throws CoreException {
		cuteVersion.copyTestFiles(srcFolder, new NullProgressMonitor());
		cuteVersion.copyHeaderFiles(cuteFolder, new NullProgressMonitor());
	}
	
	private IFolder createFolder(IProject project, String relPath)
			throws CoreException {
		IFolder folder= project.getProject().getFolder(relPath);
		if (!folder.exists()) {
			createFolder(folder, true, true, new NullProgressMonitor());
		}
				
		if(CCorePlugin.getDefault().isNewStyleProject(project.getProject())){
			ICSourceEntry newEntry = new CSourceEntry(folder, null, 0);
			ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project.getProject(), true);
			addEntryToAllCfgs(des, newEntry, false);
			CCorePlugin.getDefault().setProjectDescription(project.getProject(), des, false, new NullProgressMonitor());
		}
		return folder;
	}

	private void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
		if (!folder.exists()) {
			IContainer parent= folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder)parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}
	
	private void addEntryToAllCfgs(ICProjectDescription des, ICSourceEntry entry, boolean removeProj) throws WriteAccessException, CoreException{
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			ICConfigurationDescription cfg = cfgs[i];
			ICSourceEntry[] entries = cfg.getSourceEntries();
			entries = addEntry(entries, entry, removeProj);
			cfg.setSourceEntries(entries);
		}
	}
		
	private ICSourceEntry[] addEntry(ICSourceEntry[] entries, ICSourceEntry entry, boolean removeProj){
		Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
		for(int i = 0; i < entries.length; i++){
			if(removeProj && new Path(entries[i].getValue()).segmentCount() == 1)
				continue;
			
			set.add(entries[i]);
		}
		set.add(entry);
		return set.toArray(new ICSourceEntry[set.size()]);
	}
	
	protected ICuteHeaders getCuteVersion(String cuteVersionString) {
		SortedSet<ICuteHeaders> headers = UiPlugin.getInstalledCuteHeaders();
		for (ICuteHeaders cuteHeaders : headers) {
			if(cuteVersionString.equals(cuteHeaders.getVersionString()))
				return cuteHeaders;
		}
		
		return null;
	}

	protected void setIncludePaths(IPath cuteFolder, IProject project) throws CoreException {
		String path = "\"${workspace_loc:" + cuteFolder.toPortableString() + "}\""; //$NON-NLS-1$ //$NON-NLS-2$
		setOptionInAllConfigs(project, path, IOption.INCLUDE_PATH);
	}

	protected void setOptionInAllConfigs(IProject project, String value, int optionType)
			throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		try{
			for(IConfiguration conf : configs){
				IToolChain toolChain = conf.getToolChain();
				setOptionInConfig(value, conf, toolChain.getOptions(), toolChain, optionType);

				ITool[] tools = conf.getTools();
				for(int j=0; j<tools.length; j++) {
					setOptionInConfig(value, conf, tools[j].getOptions(), tools[j], optionType);
				}
			}
		}catch (BuildException be){
			throw new CoreException(new Status(IStatus.ERROR,CuteCorePlugin.PLUGIN_ID,42,be.getMessage(), be));
		}
	}
	
	protected void setOptionInConfig(String value, IConfiguration config,
			IOption[] options, IHoldsOptions optionHolder, int optionType) throws BuildException {
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];
			if (option.getValueType() == optionType) {
				String[] includePaths = getStrategy(optionType).getValues(option);
				String[] newPaths = new String[includePaths.length + 1];
				System.arraycopy(includePaths, 0, newPaths, 0, includePaths.length);
				newPaths[includePaths.length] = value;
				ManagedBuildManager.setOption(config, optionHolder, option, newPaths);
			}
		}
	}

	protected IFile getTestMainFile(IProject project) {
		return project.getFile("src/Test.cpp"); //$NON-NLS-1$
	}
	
	protected GetOptionsStrategy getStrategy(int optionType) {
		switch (optionType) {
		case IOption.INCLUDE_PATH:
			return new IncludePathStrategy();

		default:
			throw new IllegalArgumentException("Illegal Argument: "+optionType); //$NON-NLS-1$
		}
	}

	private class IncludePathStrategy implements GetOptionsStrategy{

		public String[] getValues(IOption option) throws BuildException {
			return option.getIncludePaths();
		}
		
	}


}
