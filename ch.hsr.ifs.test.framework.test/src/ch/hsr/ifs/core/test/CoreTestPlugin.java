package ch.hsr.ifs.core.test;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class CoreTestPlugin extends Plugin {

	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.core.test"; //$NON-NLS-1$
	private static CoreTestPlugin plugin;

	public CoreTestPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		super.start(bundleContext);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		CoreTestPlugin.plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CoreTestPlugin getDefault() {
		return plugin;
	}
}
