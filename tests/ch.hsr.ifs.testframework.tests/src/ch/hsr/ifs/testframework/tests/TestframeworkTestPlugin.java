/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 * @author Emanuel Graf IFS
 *
 */
public class TestframeworkTestPlugin extends Plugin {

   public static final String             PLUGIN_ID = "ch.hsr.ifs.cute.core.test";
   private static TestframeworkTestPlugin plugin;

   public TestframeworkTestPlugin() {
      plugin = this;
   }

   @Override
   public void start(BundleContext bundleContext) throws Exception {
      plugin = this;
      super.start(bundleContext);
   }

   @Override
   public void stop(BundleContext bundleContext) throws Exception {
      super.stop(bundleContext);
      TestframeworkTestPlugin.plugin = null;
   }

   public static TestframeworkTestPlugin getDefault() {
      return plugin;
   }
}
