/******************************************************************************
* Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/
package ch.hsr.ifs.cute.namespactor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class NamespactorActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.namespactor"; //$NON-NLS-1$

	private static NamespactorActivator plugin;
	
	public NamespactorActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public static void log(IStatus status) {
		//getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		//log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}

	public static NamespactorActivator getDefault() {
		return plugin;
	}
}
