/*******************************************************************************
 * Copyright (c) 2007-2013, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class CuteHeadersPlugin extends AbstractUIPlugin {

   public static final String PLUGIN_ID = "ch.hsr.ifs.cute.headers";

   private static CuteHeadersPlugin plugin;

   public CuteHeadersPlugin() {}

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

   public static CuteHeadersPlugin getDefault() {
      return plugin;
   }

}
