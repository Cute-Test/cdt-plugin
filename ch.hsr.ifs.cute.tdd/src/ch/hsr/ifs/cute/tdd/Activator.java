/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "ch.hsr.ifs.cute.tdd";

	private static final IPath ICONS_PATH = new Path("$nl$/icons");

	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public static Activator getDefault() {
		if (plugin == null) {
			throw new RuntimeException(Messages.Activator_0);
		}
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String relativePath) {
		IPath path = ICONS_PATH.append(relativePath);
		return createImageDescriptor(getDefault().getBundle(), path);
	}

	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url = FileLocator.find(bundle, path, null);
		return ImageDescriptor.createFromURL(url);
	}

	public ITextSelection getEditorSelection() {
		return (ITextSelection) getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getSelectionProvider()
				.getSelection();
	}
}
