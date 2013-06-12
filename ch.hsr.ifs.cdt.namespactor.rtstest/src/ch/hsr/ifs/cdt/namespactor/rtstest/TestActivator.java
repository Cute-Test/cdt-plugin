package ch.hsr.ifs.cdt.namespactor.rtstest;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TestActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "ch.hsr.ifs.namespactor.rtstestPlugin"; //$NON-NLS-1$

	private static TestActivator plugin;
	
	public TestActivator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static TestActivator getDefault() {
		return plugin;
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}
}
