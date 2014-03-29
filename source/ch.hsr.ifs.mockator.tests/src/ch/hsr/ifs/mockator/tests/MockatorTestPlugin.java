package ch.hsr.ifs.mockator.tests;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class MockatorTestPlugin extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "ch.hsr.ifs.mockator.tests";
  private static MockatorTestPlugin plugin;

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

  public static MockatorTestPlugin getDefault() {
    return plugin;
  }
}
