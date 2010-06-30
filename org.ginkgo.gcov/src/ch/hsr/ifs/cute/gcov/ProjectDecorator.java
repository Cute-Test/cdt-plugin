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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ProjectDecorator implements ILightweightLabelDecorator {

	private static final ImageDescriptor GCOV_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(GcovPlugin.PLUGIN_ID, "icons/ovr16/gcov_ovr.gif"); //$NON-NLS-1$;
	private static final ImageDescriptor GCOV_DEACT_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(GcovPlugin.PLUGIN_ID, "icons/ovr16/gcov_deact_ovr.gif"); //$NON-NLS-1$;;



	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

		

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IProject) {
			IProject proj = (IProject)element;
			try {
				if(proj.hasNature(GcovNature.NATURE_ID)) {
					
					IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
					IConfiguration config = info.getDefaultConfiguration();
					if(config.getId().equals(GcovNature.GCOV_CONFG_ID)){
						decoration.addOverlay(GCOV_ICON, IDecoration.BOTTOM_LEFT);
					}else {
						decoration.addOverlay(GCOV_DEACT_ICON, IDecoration.BOTTOM_LEFT);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
