/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


public class CuteUIPlugin extends AbstractUIPlugin {

   public static final String  PLUGIN_ID  = "ch.hsr.ifs.cute.ui";
   private static CuteUIPlugin plugin;
   private static final IPath  ICONS_PATH = new Path("$nl$/icons");

   public CuteUIPlugin() {}

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

   public static CuteUIPlugin getDefault() {
      return plugin;
   }

   public static ImageDescriptor getImageDescriptor(String relativePath) {
      IPath path = ICONS_PATH.append(relativePath);
      return createImageDescriptor(getDefault().getBundle(), path);
   }

   public static void log(IStatus status) {
      getDefault().getLog().log(status);
   }

   public static void log(String msg) {
      log(new Status(IStatus.ERROR, PLUGIN_ID, msg));
   }

   public static void log(String msg, Throwable e) {
      log(new Status(IStatus.ERROR, PLUGIN_ID, 1, msg, e));
   }

   public static void log(Throwable e) {
      log("Internal Error", e);
   }

   private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
      URL url = FileLocator.find(bundle, path, null);
      return ImageDescriptor.createFromURL(url);
   }

}
