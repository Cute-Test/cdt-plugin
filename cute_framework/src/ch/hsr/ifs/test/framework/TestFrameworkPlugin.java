/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.test.framework;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.test.framework.model.CuteModel;
import ch.hsr.ifs.test.framework.ui.FallbackImageProvider;
import ch.hsr.ifs.test.framework.ui.FallbackMessages;

/**
 * The activator class controls the plug-in life cycle
 */
public class TestFrameworkPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.hsr.ifs.test.framework"; //$NON-NLS-1$

	// The shared instance
	private static TestFrameworkPlugin plugin;
	
	private static final IPath ICONS_PATH= new Path("$nl$/icons"); //$NON-NLS-1$
	
	private final CuteModel model = new CuteModel();
		
	/**
	 * The constructor
	 */
	public TestFrameworkPlugin() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

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
	public static TestFrameworkPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
	
	public static ImageDescriptor getImageDescriptor(String relativePath) {
		IPath path= ICONS_PATH.append(relativePath);
		return createImageDescriptor(getDefault().getBundle(), path);
	}
	
	public static ImageProvider getImageProvider() {
		try{
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "ImageProvider"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension extension2 : extensions) {
					IConfigurationElement[] configElements = extension2.getConfigurationElements();
					String className =configElements[0].getAttribute("class"); //$NON-NLS-1$
					Class<?> obj = getDefault().getBundle().loadClass(className);
					return (ImageProvider) obj.newInstance();
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
		return new FallbackImageProvider();
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (plugin == null)
			return null;
		IWorkbench workBench= plugin.getWorkbench();
		if (workBench == null)
			return null;
		return workBench.getActiveWorkbenchWindow();
	}
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow= getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return null;
		return activeWorkbenchWindow.getActivePage();
	}
	
	/**
	 * @since 3.0
	 */
	public static Messages getMessages(){
		try{
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "Messages"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension extension2 : extensions) {
					IConfigurationElement[] configElements = extension2.getConfigurationElements();
					String className =configElements[0].getAttribute("class"); //$NON-NLS-1$
					Class<?> obj = getDefault().getBundle().loadClass(className);
					return (Messages) obj.newInstance();
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
		return new FallbackMessages();		
	}
	
	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url= FileLocator.find(bundle, path, null);
		return ImageDescriptor.createFromURL(url);
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	//PDE runtime:Error Log view
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static CuteModel getModel() {
		return getDefault().model;
	}

}
