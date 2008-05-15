/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import ch.hsr.ifs.cutelauncher.ui.CustomisedLaunchConfigTab;

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
			cElement = getContext(configuration, getPlatform(configuration));
			if (cElement != null) {
				cElement = cElement.getCProject();
			
				LaunchEnvironmentVariables.apply(configuration, cElement.getCProject());
			}
		}catch(CoreException ce){
			CuteLauncherPlugin.log(ce);
		}
		super.setDefaults(configuration);
	}
	///////////////////////////////////////////
	// direct copy and paste from 
	// @see org.eclipse.cdt.launch.ui.CLaunchConfigurationTab
	// this is to hunt for current CProject
	//////////////////////////////////////////
	protected ICElement getContext(ILaunchConfiguration config, String platform) {
		String projectName = null;
		String programName = null;
		IWorkbenchPage page = LaunchUIPlugin.getActivePage();
		Object obj = null;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
		} catch (CoreException e) {
		}
		if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
			if (cProject != null && cProject.exists()) {
				obj = cProject;
			}
		} else {
			if (page != null) {
				ISelection selection = page.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection)selection;
					if (!ss.isEmpty()) {
						obj = ss.getFirstElement();
					}
				}
			}
		}
		if (obj instanceof IResource) {
			ICElement ce = CoreModel.getDefault().create((IResource)obj);
			if (ce == null) {
				IProject pro = ((IResource)obj).getProject();
				ce = CoreModel.getDefault().create(pro);
			}
			obj = ce;
		}
		if (obj instanceof ICElement) {
			if (platform != null && !platform.equals("*")) { //$NON-NLS-1$
				ICDescriptor descriptor;
				try {
					descriptor = CCorePlugin.getDefault().getCProjectDescription( ((ICElement)obj).getCProject().getProject(),
							false);
					if (descriptor != null) {
						String projectPlatform = descriptor.getPlatform();
						if (!projectPlatform.equals(platform) && !projectPlatform.equals("*")) { //$NON-NLS-1$
							obj = null;
						}
					}
				} catch (CoreException e) {
				}
			}
			if (obj != null) {
				if (programName == null || programName.equals("")) { //$NON-NLS-1$
					return (ICElement)obj;
				}
				ICElement ce = (ICElement)obj;
				IProject project;
				project = (IProject)ce.getCProject().getResource();
				IPath programFile = project.getFile(programName).getLocation();
				ce = CCorePlugin.getDefault().getCoreModel().create(programFile);
				if (ce != null && ce.exists()) {
					return ce;
				}
				return (ICElement)obj;
			}
		}
		IEditorPart part = page.getActiveEditor();
		if (part != null) {
			IEditorInput input = part.getEditorInput();
			return (ICElement)input.getAdapter(ICElement.class);
		}
		return null;
	}

	protected String getPlatform(ILaunchConfiguration config) {
		String platform = Platform.getOS();
		try {
			return config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PLATFORM, platform);
		} catch (CoreException e) {
			return platform;
		}
	}
	///////////////////////////////////////////
	
	
}
//refer to CMainTab.initializeProgramName()
//for the launch configuration name being appended with "Debug" word. 
