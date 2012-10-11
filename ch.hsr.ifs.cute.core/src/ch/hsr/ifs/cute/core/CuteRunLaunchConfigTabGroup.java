/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.core;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.core.launch.LaunchEnvironmentVariables;
import ch.hsr.ifs.test.framework.launch.CustomisedLaunchConfigTab;

public class CuteRunLaunchConfigTabGroup extends
		AbstractLaunchConfigurationTabGroup {
	
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new CMainTab(true),
				new CArgumentsTab(),
				new org.eclipse.debug.ui.EnvironmentTab(),
				new CommonTab(),
				new CustomisedLaunchConfigTab()
			};
			setTabs(tabs);
	}

	//set default values for environment variables during the very first create
	//trigger via Run as Dialog>new launch configuration button
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
		try{
			ICElement cElement = null;
			proxyCLaunchConfigurationTab t=new proxyCLaunchConfigurationTab();
			cElement = t.getContext1(configuration, t.getPlatform1(configuration));
			if (cElement != null) {
				cElement = cElement.getCProject();
			
				LaunchEnvironmentVariables.apply(configuration, cElement.getCProject());
			}
		}catch(CoreException ce){
			CuteCorePlugin.log(ce);
		}
		super.setDefaults(configuration);
	}

	// @see org.eclipse.cdt.launch.ui.CLaunchConfigurationTab
	// this is to hunt for current CProject
	class proxyCLaunchConfigurationTab extends CLaunchConfigurationTab{
		public void performApply(ILaunchConfigurationWorkingCopy configuration){}
		public void createControl(Composite parent){}
		public void initializeFrom(ILaunchConfiguration configuration){}		
		public String getName(){return "";} //$NON-NLS-1$
		public void setDefaults(ILaunchConfigurationWorkingCopy configuration){}
		
		public ICElement getContext1(ILaunchConfiguration config, String platform){
			return this.getContext(config,platform);
		}
		public String getPlatform1(ILaunchConfiguration config){
			return this.getPlatform(config);
		}
		
	}
	
}
//refer to CMainTab.initializeProgramName()
//for the launch configuration name being appended with "Debug" word. 
