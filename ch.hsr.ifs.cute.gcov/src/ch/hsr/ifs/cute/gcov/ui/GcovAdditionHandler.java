/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.ui;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class GcovAdditionHandler implements ICuteWizardAdditionHandler {

	public static final String GCOV_CONFG_ID = "gcov";

	private static final String MACOSX_LINKER_OPTION_FLAGS = "macosx.cpp.link.option.flags";
	private static final String GCOV_LINKER_FLAGS = "-fprofile-arcs -ftest-coverage -std=c99";
	private static final String GNU_CPP_LINK_OPTION_FLAGS = "gnu.cpp.link.option.flags";
	private static final String GNU_CPP_LINKER_ID = "cdt.managedbuild.tool.gnu.cpp.linker";
	private static final String MAC_CPP_LINKER_ID = "cdt.managedbuild.tool.macosx.cpp.linker";
	private static final String GNU_C_COMPILER_OPTION_MISC_OTHER = "gnu.c.compiler.option.misc.other";
	private static final String GNU_C_COMPILER_ID = "cdt.managedbuild.tool.gnu.c.compiler";
	private static final String GNU_CPP_COMPILER_OPTION_OTHER_OTHER = "gnu.cpp.compiler.option.other.other";
	private static final String GNU_CPP_COMPILER_ID = "cdt.managedbuild.tool.gnu.cpp.compiler";
	private static final String GCOV_C_COMPILER_FLAGS = "-fprofile-arcs -ftest-coverage -std=c99 ";
	private static final String GCOV_CPP_COMPILER_FLAGS = "-fprofile-arcs -ftest-coverage ";
	private static final String GCOV_CPP_COMPILER_LIB_FLAGS = GCOV_CPP_COMPILER_FLAGS + "-lgcov ";
	private static final String GCOV_C_COMPILER_LIB_FLAGS = GCOV_C_COMPILER_FLAGS + "-lgcov ";

	private GcovWizardAddition addition;

	public GcovAdditionHandler() {
	}

	public GcovAdditionHandler(GcovWizardAddition addition) {
		super();
		this.addition = addition;
	}

	public void configureProject(IProject project, IProgressMonitor pm) throws CoreException {
		SubMonitor mon = SubMonitor.convert(pm, 2);
		if (isGcovEnabled()) {
			GcovNature.addGcovNature(project, mon);
			addGcovConfig(project);
		}
		mon.done();
	}

	protected boolean isGcovEnabled() {
		return addition != null ? addition.enableGcov : true;
	}

	public IConfiguration addGcovConfig(IProject project) throws CoreException {
		try {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			for (IConfiguration config : configs) {
				if (config.getParent().getId().contains("debug")) {
					IConfiguration newConfig = info.getManagedProject().createConfigurationClone(config, GCOV_CONFG_ID);
					newConfig.setName("Debug Gcov");
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
			throw new CoreException(new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	public IConfiguration addGcovLibConfig(IProject project) throws CoreException {
		try {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			for (IConfiguration config : configs) {
				if (config.getParent().getId().contains("debug")) {
					IConfiguration newConfig = info.getManagedProject().createConfigurationClone(config, GCOV_CONFG_ID);
					newConfig.setName("Debug Gcov");
					setOptionInTool(newConfig, GNU_CPP_COMPILER_ID, GNU_CPP_COMPILER_OPTION_OTHER_OTHER, GCOV_CPP_COMPILER_LIB_FLAGS);
					setOptionInTool(newConfig, GNU_C_COMPILER_ID, GNU_C_COMPILER_OPTION_MISC_OTHER, GCOV_C_COMPILER_LIB_FLAGS);
					setOptionInTool(newConfig, GNU_CPP_LINKER_ID, GNU_CPP_LINK_OPTION_FLAGS, GCOV_LINKER_FLAGS);
					setOptionInTool(newConfig, MAC_CPP_LINKER_ID, MACOSX_LINKER_OPTION_FLAGS, GCOV_LINKER_FLAGS);
					ManagedBuildManager.setDefaultConfiguration(project, newConfig);
					ManagedBuildManager.setSelectedConfiguration(project, newConfig);

					return newConfig;
				}
			}
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	private void setOptionInTool(IConfiguration config, String toolId, String optionId, String optionValue) throws BuildException {
		ITool[] tools = config.getToolsBySuperClassId(toolId);
		for (ITool tool : tools) {
			IOption option = tool.getOptionById(optionId);
			String value = option.getDefaultValue() == null ? optionValue : option.getDefaultValue().toString().trim() + " " + optionValue;
			ManagedBuildManager.setOption(config, tool, option, value);
		}
	}

	public void configureLibProject(IProject libProject) throws CoreException {
		if (isGcovEnabled() && libProjectNeedGcovConfig(libProject)) {
			addGcovLibConfig(libProject);
			BuildAction buildAction = new BuildAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
			buildAction.selectionChanged(new StructuredSelection(libProject));
			buildAction.run();
		}
	}

	private boolean libProjectNeedGcovConfig(IProject libProject) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
		for (String name : info.getConfigurationNames()) {
			if (name.equalsIgnoreCase("debug gcov")) {
				return false;
			}
		}
		return true;
	}

}