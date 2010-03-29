/*******************************************************************************
 * Copyright (c) 2007, 2010 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.cute.ui.project.headers.CuteHeaderComparator;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * The activator class controls the plug-in life cycle
 */
public class UiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.ui"; //$NON-NLS-1$

	// The shared instance
	private static UiPlugin plugin;
	
	private static final IPath ICONS_PATH= new Path("$nl$/icons"); //$NON-NLS-1$

	public static final String CUTE_VERSION_PROPERTY_NAME = "cuteVersion";
	
	/**
	 * The constructor
	 */
	public UiPlugin() {
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

	public static ICuteHeaders getCuteVersion(String cuteVersionString) {
		SortedSet<ICuteHeaders> headers = getInstalledCuteHeaders();
		for (ICuteHeaders cuteHeaders : headers) {
			if(cuteVersionString.equals(cuteHeaders.getVersionString()))
				return cuteHeaders;
		}
		
		return null;
	}

	public static ICuteHeaders getCuteVersionString(IProject project) throws CoreException {
		QualifiedName key = new QualifiedName(PLUGIN_ID, CUTE_VERSION_PROPERTY_NAME);
		String versionString = project.getPersistentProperty(key);
		if(versionString != null) {
			return getCuteVersion(versionString);
		}else { //find out version by parsing the version header
			IResource res = project.findMember("cute/cute_version.h");
			if (res instanceof IFile) {
				String cuteVersionstring = "";
				IFile file = (IFile) res;
				BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
				String line;
				try {
					Pattern versPtr = Pattern.compile("#define CUTE_LIB_VERSION \\\"(\\d\\.\\d\\.\\d)\\\"$");
					while((line = br.readLine()) != null	) {
						Matcher matcher = versPtr.matcher(line);
						if(matcher.matches()) {
							cuteVersionstring = "Cute Headers " + matcher.group(1);
							project.setPersistentProperty(key, cuteVersionstring);
							break;
						}
					}
					return getCuteVersion(cuteVersionstring);
				} catch (IOException e) {
				}
			}
			//fallback
			return getInstalledCuteHeaders().first();
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UiPlugin getDefault() {
		return plugin;
	}
	
	
	public static ImageDescriptor getImageDescriptor(String relativePath) {
		IPath path= ICONS_PATH.append(relativePath);
		return createImageDescriptor(getDefault().getBundle(), path);
	}
	
	public static SortedSet<ICuteHeaders> getInstalledCuteHeaders(){
		SortedSet<ICuteHeaders> headers = new TreeSet<ICuteHeaders>(new CuteHeaderComparator());
		try{
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(UiPlugin.PLUGIN_ID, "Headers"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension extension2 : extensions) {
					IConfigurationElement[] configElements = extension2.getConfigurationElements();
					String className =configElements[0].getAttribute("class"); //$NON-NLS-1$
					Class<?> obj = Platform.getBundle(extension2.getContributor().getName()).loadClass(className);
					headers.add((ICuteHeaders) obj.newInstance());
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
		return headers;
	}
	
	
	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url= FileLocator.find(bundle, path, null);
		return ImageDescriptor.createFromURL(url);
	}

}
