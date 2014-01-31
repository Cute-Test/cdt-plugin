/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TDDTestPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "ch.hsr.eclipse.cdt.ui.tests";

	private static TDDTestPlugin plugin;

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

	public static TDDTestPlugin getDefault() {
		return plugin;
	}

	public static void logStatus(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String msg) {
		logStatus(new Status(IStatus.ERROR, PLUGIN_ID, msg));
	}
}
