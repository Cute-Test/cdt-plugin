/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.core.headers.CuteHeaders;


public class CuteUIPlugin extends AbstractUIPlugin {

   public static final String           PLUGIN_ID                  = "ch.hsr.ifs.cute.ui";
   private static CuteUIPlugin          plugin;
   private static final IPath           ICONS_PATH                 = new Path("$nl$/icons");
   public static final QualifiedName    CUTE_VERSION_PROPERTY_NAME = new QualifiedName(PLUGIN_ID, "cuteVersion");
   public static SortedSet<CuteHeaders> installedHeaders;

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

   public static CuteHeaders getCuteVersion(String cuteVersionString) {
      SortedSet<CuteHeaders> headers = getInstalledCuteHeaders();
      for (CuteHeaders cuteHeaders : headers) {
         if (cuteVersionString.equals(cuteHeaders.getVersionString())) return cuteHeaders;
      }
      return null;
   }

   public static CuteHeaders getCuteVersion(IProject project) throws CoreException {
      String versionString = project.getPersistentProperty(CUTE_VERSION_PROPERTY_NAME);
      if (versionString != null) {
         return getCuteVersion(versionString);
      } else { // find out version by parsing the version header
         IResource res = project.findMember("cute/cute_version.h");
         if (res instanceof IFile) {
            String cuteVersionstring = "";
            IFile file = (IFile) res;
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
            String line;
            try {
               Pattern versPtr = Pattern.compile("#define CUTE_LIB_VERSION \\\"(\\d\\.\\d\\.\\d)\\\"$");
               while ((line = br.readLine()) != null) {
                  Matcher matcher = versPtr.matcher(line);
                  if (matcher.matches()) {
                     cuteVersionstring = "CUTE Headers " + matcher.group(1);
                     project.setPersistentProperty(CUTE_VERSION_PROPERTY_NAME, cuteVersionstring);
                     break;
                  }
               }
               return getCuteVersion(cuteVersionstring);
            } catch (IOException e) {} finally {
               try {
                  br.close();
               } catch (IOException e) {}
            }
         }
         return getInstalledCuteHeaders().first();
      }
   }

   public static CuteUIPlugin getDefault() {
      return plugin;
   }

   public static ImageDescriptor getImageDescriptor(String relativePath) {
      IPath path = ICONS_PATH.append(relativePath);
      return createImageDescriptor(getDefault().getBundle(), path);
   }

   public static synchronized SortedSet<CuteHeaders> getInstalledCuteHeaders() {
      if (installedHeaders == null) {
         installedHeaders = new TreeSet<>();
         IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CuteCorePlugin.PLUGIN_ID, "Headers");
         if (extensionPoint != null) {
            IExtension[] extensions = extensionPoint.getExtensions();
            for (IExtension extension : extensions) {
               addHeaderInstance(extension);
            }
         }
      }
      return new TreeSet<>(installedHeaders);
   }

   private static void addHeaderInstance(IExtension extension) {
      try {
         IConfigurationElement[] configElements = extension.getConfigurationElements();
         String className = configElements[0].getAttribute("class");
         Class<?> obj = Platform.getBundle(extension.getContributor().getName()).loadClass(className);
         installedHeaders.add((CuteHeaders) obj.getDeclaredConstructor().newInstance());
      } catch (AbstractMethodError e) {
         // igonoring outdated (incompatible) old header plugins
         // (which might still be installed when updating to current cute version
      } catch (Exception ignored) {}
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