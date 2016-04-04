package ch.hsr.ifs.cute.templator.plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.cute.templator.plugin.util.ColorPalette;

/**
 * The activator class controls the plug-in life cycle
 */
public class TemplatorPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.templator.plugin"; //$NON-NLS-1$

	// The shared instance
	private static TemplatorPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ColorPalette.disposePalette();
		plugin = null;
		super.stop(context);
	}

	public static TemplatorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
