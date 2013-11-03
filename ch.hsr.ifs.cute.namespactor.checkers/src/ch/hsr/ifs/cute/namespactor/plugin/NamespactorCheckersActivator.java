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
package ch.hsr.ifs.cute.namespactor.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class NamespactorCheckersActivator extends AbstractUIPlugin {

	private static BundleContext context;
	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.namespactor.checkers";
	private static NamespactorCheckersActivator plugin;

	static BundleContext getContext() {
		return context;
	}

	public static NamespactorCheckersActivator getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		NamespactorCheckersActivator.context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		NamespactorCheckersActivator.context = null;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, "Internal Error", e));
	}

	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}
}
