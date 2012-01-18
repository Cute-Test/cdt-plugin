/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
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

import ch.hsr.ifs.cute.gcov.ui.GcovAdditionHandler;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ProjectDecorator implements ILightweightLabelDecorator{

	private static final ImageDescriptor GCOV_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(GcovPlugin.PLUGIN_ID, "icons/ovr16/gcov_ovr.gif"); //$NON-NLS-1$;
	private static final ImageDescriptor GCOV_DEACT_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(GcovPlugin.PLUGIN_ID, "icons/ovr16/gcov_deact_ovr.gif"); //$NON-NLS-1$;;


	public void addListener(ILabelProviderListener listener) {}

	public void dispose() {}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {}

		

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IProject) {
			IProject proj = (IProject)element;
			try {
				if(proj.exists() && proj.isOpen() && proj.hasNature(GcovNature.NATURE_ID)) {					
					IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
					IConfiguration config = info.getDefaultConfiguration();
					if(config.getId().equals(GcovAdditionHandler.GCOV_CONFG_ID)){
						decoration.addOverlay(GCOV_ICON, IDecoration.BOTTOM_RIGHT);
					}else {
						decoration.addOverlay(GCOV_DEACT_ICON, IDecoration.BOTTOM_RIGHT);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
