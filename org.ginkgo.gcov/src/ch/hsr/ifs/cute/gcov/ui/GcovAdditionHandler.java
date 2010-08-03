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
	
	public static final String GCOV_CONFG_ID = "gcov"; //$NON-NLS-1$
	
	private static final String MACOSX_LINKER_OPTION_FLAGS = "macosx.cpp.link.option.flags"; //$NON-NLS-1$
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
	
	
	private GcovWizardAddition addition;
	
	public GcovAdditionHandler() {}

	public GcovAdditionHandler(GcovWizardAddition addition) {
		super();
		this.addition = addition;
	}

	public void configureProject(IProject project, IProgressMonitor pm) throws CoreException {
		SubMonitor mon = SubMonitor.convert(pm, 2);
		if(isGcovEnabled()) {
			GcovNature.addGcovNature(project, mon);
			addGcovConfig(project);	
		}
		mon.done();
	}

	protected boolean isGcovEnabled() {
		return addition!= null ? addition.enableGcov : true;
	}
	
	public IConfiguration addGcovConfig(IProject project) throws CoreException {
		try {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			if(config.getParent().getId().contains("debug")) { //$NON-NLS-1$
				IConfiguration newConfig = info.getManagedProject().createConfigurationClone(config, GCOV_CONFG_ID);
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

	private void setOptionInTool(IConfiguration config, String toolId, String optionId, String optionValue)
	throws BuildException {
		ITool[] tools = config.getToolsBySuperClassId(toolId);
		for (ITool tool : tools) {
			IOption option = tool.getOptionById(optionId);
			String value = option.getDefaultValue() == null ? optionValue : option.getDefaultValue().toString().trim() + " " + optionValue; //$NON-NLS-1$
			ManagedBuildManager.setOption(config, tool, option, value);
		}
	}

	public void configureLibProject(IProject libProject) throws CoreException {
		if(isGcovEnabled() && libProjectNeedGcovConfig(libProject)) {
			addGcovConfig(libProject);
			BuildAction buildAction = new BuildAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
			buildAction.selectionChanged(new StructuredSelection(libProject));
			buildAction.run();
		}
	}
	
	private boolean libProjectNeedGcovConfig(IProject libProject) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
		for (String name : info.getConfigurationNames()) {
			if(name.equalsIgnoreCase("debug gcov")) { //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

}
