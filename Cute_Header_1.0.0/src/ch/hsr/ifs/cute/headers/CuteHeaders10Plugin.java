package ch.hsr.ifs.cute.headers;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @since 2.0
 */
public class CuteHeaders10Plugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.headers.1_0"; //$NON-NLS-1$

	// The shared instance
	private static CuteHeaders10Plugin plugin;
	
	/**
	 * The constructor
	 */
	public CuteHeaders10Plugin() {
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
	public static CuteHeaders10Plugin getDefault() {
		return plugin;
	}

}
