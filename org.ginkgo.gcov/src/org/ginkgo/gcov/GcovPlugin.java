package org.ginkgo.gcov;

import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GcovPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.ginkgo.gcov";

	// The shared instance
	private static GcovPlugin plugin;
	private ArrayList<IPropertyChangeListener> myListeners;
	/**
	 * The constructor
	 */
	public GcovPlugin() {
		myListeners=new ArrayList<IPropertyChangeListener>();
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
	// A public method that allows listener registration
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if(!myListeners.contains(listener))
			myListeners.add(listener);
	}

	// A public method that allows listener registration
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		myListeners.remove(listener);
	}
	public ArrayList<IPropertyChangeListener> getMyListeners() {
		return myListeners;
	}
}
