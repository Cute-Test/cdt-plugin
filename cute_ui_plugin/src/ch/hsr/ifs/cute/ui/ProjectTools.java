/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import ch.hsr.ifs.cute.core.CuteCorePlugin;


/**
 * @author Emanuel Graf IFS
 *
 */
public class ProjectTools {

	public static  IFolder createFolder(IProject project, String relPath, boolean addToInclude)
			throws CoreException {
		IFolder folder= project.getProject().getFolder(relPath);
		if (!folder.exists()) {
			ProjectTools.createFolder(folder, true, true, new NullProgressMonitor());
		}
				
		if(addToInclude && CCorePlugin.getDefault().isNewStyleProject(project.getProject())){
			ICSourceEntry newEntry = new CSourceEntry(folder, null, 0);
			ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project.getProject(), true);
			ProjectTools.addEntryToAllCfgs(des, newEntry, false);
			CCorePlugin.getDefault().setProjectDescription(project.getProject(), des, false, new NullProgressMonitor());
		}
		return folder;
	}

	public static void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
		if (!folder.exists()) {
			IContainer parent= folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder)parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}

	public static void addEntryToAllCfgs(ICProjectDescription des, ICSourceEntry entry, boolean removeProj) throws WriteAccessException, CoreException{
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			ICConfigurationDescription cfg = cfgs[i];
			ICSourceEntry[] entries = cfg.getSourceEntries();
			entries = addEntry(entries, entry, removeProj);
			cfg.setSourceEntries(entries);
		}
	}

	public static ICSourceEntry[] addEntry(ICSourceEntry[] entries, ICSourceEntry entry, boolean removeProj){
		Set<ICSourceEntry> set = new HashSet<ICSourceEntry>();
		for(int i = 0; i < entries.length; i++){
			if(removeProj && new Path(entries[i].getValue()).segmentCount() == 1)
				continue;
			
			set.add(entries[i]);
		}
		set.add(entry);
		return set.toArray(new ICSourceEntry[set.size()]);
	}

	public static void setOptionInConfig(String value, IConfiguration config,
			IOption[] options, IHoldsOptions optionHolder, int optionType, IIncludeStrategyProvider inStratProv) throws BuildException {
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];
			if (option.getValueType() == optionType) {
				String[] includePaths = inStratProv.getStrategy(optionType).getValues(option);
				String[] newPaths = new String[includePaths.length + 1];
				System.arraycopy(includePaths, 0, newPaths, 0, includePaths.length);
				newPaths[includePaths.length] = value;
				ManagedBuildManager.setOption(config, optionHolder, option, newPaths);
			}
		}
	}

	public static void setOptionInAllConfigs(IProject project, String value, int optionType, IIncludeStrategyProvider inclStratProv)
			throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		try{
			for(IConfiguration conf : configs){
				IToolChain toolChain = conf.getToolChain();
				setOptionInConfig(value, conf, toolChain.getOptions(), toolChain, optionType, inclStratProv);
	
				ITool[] tools = conf.getTools();
				for(int j=0; j<tools.length; j++) {
					setOptionInConfig(value, conf, tools[j].getOptions(), tools[j], optionType, inclStratProv);
				}
			}
		}catch (BuildException be){
			throw new CoreException(new Status(IStatus.ERROR,CuteCorePlugin.PLUGIN_ID,42,be.getMessage(), be));
		}
	}

	public static void setIncludePaths(IPath cuteFolder, IProject project, IIncludeStrategyProvider inclStratProv) throws CoreException {
		String path = "\"${workspace_loc:" + cuteFolder.toPortableString() + "}\""; //$NON-NLS-1$ //$NON-NLS-2$
		setOptionInAllConfigs(project, path, IOption.INCLUDE_PATH, inclStratProv);
	}

}