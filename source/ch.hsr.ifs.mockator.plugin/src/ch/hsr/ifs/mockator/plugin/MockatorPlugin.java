package ch.hsr.ifs.mockator.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.iltis.core.exception.ILTISException;



public class MockatorPlugin extends AbstractUIPlugin {

   public static final String    PLUGIN_ID = "ch.hsr.ifs.mockator.plugin";
   private static MockatorPlugin plugin;

   @Override
   public void start(final BundleContext context) throws Exception {
      super.start(context);
      plugin = this;
   }

   @Override
   public void stop(final BundleContext context) throws Exception {
      plugin = null;
      super.stop(context);
   }

   public static MockatorPlugin getDefault() {
      final Object object = plugin;
      ILTISException.Unless.notNull(object, "Plugin not active, access not possible");
      return plugin;
   }

   public static void logMsg(final String msg) {
      getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, 0, msg, null));
   }
}
