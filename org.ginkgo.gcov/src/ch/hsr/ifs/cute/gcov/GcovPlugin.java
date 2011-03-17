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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.cute.gcov.model.CoverageModel;

/**
 * The activator class controls the plug-in life cycle
 */
public class GcovPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.gcov"; //$NON-NLS-1$

	// The shared instance
	private static GcovPlugin plugin;

	public static final String GCOV_MARKER_TYPE = "ch.hsr.ifs.cute.gcov.coverageMarker"; //$NON-NLS-1$
	public static final String UNCOVER_MARKER_TYPE = "ch.hsr.ifs.cute.gcov.lineUnCoverMarker"; //$NON-NLS-1$
	public static final String COVER_MARKER_TYPE = "ch.hsr.ifs.cute.gcov.lineCoverMarker"; //$NON-NLS-1$
	public static final String PARTIALLY_MARKER_TYPE = "ch.hsr.ifs.cute.gcov.linePartialCoverMarker"; //$NON-NLS-1$
	private CoverageModel cModel = new CoverageModel();
	
	/**
	 * The constructor
	 */
	public GcovPlugin() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListner(), IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GcovPlugin getDefault() {
		return plugin;
	}

	public CoverageModel getcModel() {
		return cModel;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	//PDE runtime:Error Log view
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

}
