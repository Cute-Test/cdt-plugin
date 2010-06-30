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
package ch.hsr.ifs.cute.gcov;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * @author Emanuel Graf IFS
 *
 */
public class GcovNature implements IProjectNature {

	public static final String GCOV_CONFG_ID = "gcov";

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "ch.hsr.ifs.cute.gcov.GcovNature";
	
	private static final String MACOSX_LINKER_OPTION_FLAGS = "macosx.cpp.link.option.flags";
	private static final String GCOV_LINKER_FLAGS = "-fprofile-arcs -ftest-coverage -std=c99"; //$NON-NLS-1$
	private static final String GNU_CPP_LINK_OPTION_FLAGS = "gnu.cpp.link.option.flags"; //$NON-NLS-1$
	private static final String GNU_CPP_LINKER_ID = "cdt.managedbuild.tool.gnu.cpp.linker"; //$NON-NLS-1$
	private static final String MAC_CPP_LINKER_ID = "cdt.managedbuild.tool.macosx.cpp.linker"; //$NON-NLS-1$
	private static final String GNU_C_COMPILER_OPTION_MISC_OTHER = "gnu.c.compiler.option.misc.other"; //$NON-NLS-1$
	private static final String GNU_C_COMPILER_ID = "cdt.managedbuild.tool.gnu.c.compiler"; //$NON-NLS-1$
	private static final String GNU_CPP_COMPILER_OPTION_OTHER_OTHER = "gnu.cpp.compiler.option.other.other"; //$NON-NLS-1$
	private static final String GNU_CPP_COMPILER_ID = "cdt.managedbuild.tool.gnu.cpp.compiler"; //$NON-NLS-1$
	private static final String GCOV_C_COMPILER_FLAGS = "-fprofile-arcs -ftest-coverage -std=c99 "; //$NON-NLS-1$
	private static final String GCOV_CPP_COMPILER_FLAGS = "-fprofile-arcs -ftest-coverage "; //$NON-NLS-1$

	private IProject project;
	
	public static void addGcovNature(IProject project, IProgressMonitor mon) throws CoreException {
		addNature(project, NATURE_ID, mon);
	}

	public static void removeCuteNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, NATURE_ID, mon);
	}

	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param proj
	 *            the project to add the nature
	 * @param natureId
	 *            the id of the nature to assign to the project
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 *  
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		for (int i = 0; i < prevNatures.length; i++) {
			if (natureId.equals(prevNatures[i]))
				return;
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = GcovNature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param project
	 *            the project to remove the nature from
	 * @param natureId
	 *            the nature id to remove
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		List<String> newNatures = new ArrayList<String>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}
	
	public static IConfiguration addGcovConfig(IProject project) throws CoreException {
		try {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			if(config.getParent().getId().contains("debug")) { //$NON-NLS-1$
				IConfiguration newConfig = info.getManagedProject().createConfigurationClone(config, GCOV_CONFG_ID); //$NON-NLS-1$
				newConfig.setName("Debug Gcov"); //$NON-NLS-1$
				setOptionInTool(newConfig, GNU_CPP_COMPILER_ID, GNU_CPP_COMPILER_OPTION_OTHER_OTHER, GCOV_CPP_COMPILER_FLAGS);
				setOptionInTool(newConfig, GNU_C_COMPILER_ID, GNU_C_COMPILER_OPTION_MISC_OTHER, GCOV_C_COMPILER_FLAGS);
				setOptionInTool(newConfig, GNU_CPP_LINKER_ID, GNU_CPP_LINK_OPTION_FLAGS, GCOV_LINKER_FLAGS);
				setOptionInTool(newConfig, MAC_CPP_LINKER_ID, MACOSX_LINKER_OPTION_FLAGS, GCOV_LINKER_FLAGS);
				ManagedBuildManager.setDefaultConfiguration(project, newConfig);
				ManagedBuildManager.setSelectedConfiguration(project, newConfig);
				
				return newConfig;
			}
		}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR,GcovPlugin.PLUGIN_ID,e.getMessage(),e));
		}
		return null;
	}

	private static void setOptionInTool(IConfiguration config, String toolId, String optionId, String optionValue)
	throws BuildException {
		ITool[] tools = config.getToolsBySuperClassId(toolId);
		for (ITool tool : tools) {
			IOption option = tool.getOptionById(optionId);
			String value = option.getDefaultValue() == null ? optionValue : option.getDefaultValue().toString().trim() + " " + optionValue; //$NON-NLS-1$
			ManagedBuildManager.setOption(config, tool, option, value);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	public void configure() throws CoreException {
		configureBuilder(GcovBuilder.BUILDER_ID);
	}
	
	private void configureBuilder(String builderID) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(builderID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		deconfigureBuilder(commands, description, GcovBuilder.BUILDER_ID);
	}
	
	private void deconfigureBuilder(ICommand[] commands,
			IProjectDescription description, String builderId) {
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				return;
			}
		}
	}

}
