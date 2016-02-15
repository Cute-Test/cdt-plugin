package ch.hsr.ifs.cute.constificator.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	private static BundleContext context;
	private static Activator activator;
	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.constificator";

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		activator = this;
	}

	public static Activator getDefault() {
		return activator;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;

	}

	public static void log(final String message) {
		activator.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}

}
