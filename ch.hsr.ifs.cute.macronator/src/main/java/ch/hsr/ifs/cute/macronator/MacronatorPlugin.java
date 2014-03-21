package ch.hsr.ifs.cute.macronator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class MacronatorPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "ch.hsr.ifs.macronator.plugin"; //$NON-NLS-1$
    public static final QualifiedName SUPPRESSED_MACROS = new QualifiedName(PLUGIN_ID, "suppressed_macros");

    // The shared instance
    private static MacronatorPlugin plugin;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static MacronatorPlugin getDefault() {
        return plugin;
    }

    /**
     * Logs the specified status with this plug-in's log.
     * 
     * @param status
     *            status to log
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Logs an internal error with the specified throwable
     * 
     * @param e
     *            the exception to be logged
     */
    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, 1, "Internal Error", e));
    }

    /**
     * Logs an error with the specified throwable and message
     * 
     * @param e
     *            the exception to be logged
     * @param message
     *            additional message
     */
    public static void log(Throwable e, String message) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, e));
    }

    /**
     * Logs an internal error with the specified message.
     * 
     * @param message
     *            the error message to log
     */
    public static void log(String message) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
    }

    public static String getDefaultPreferenceValue(QualifiedName name) {
        return (name.equals(SUPPRESSED_MACROS)) ? ".suppressed" : "";
    }

}
