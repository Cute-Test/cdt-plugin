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

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	public class ActivationListener implements BundleListener {
		@Override
		public void bundleChanged(BundleEvent event) {
			if (!event.getBundle().getSymbolicName().equals("ch.hsr.eclipse.cdt") && event.getType() == BundleEvent.STARTED) { //$NON-NLS-1$
				return;
			}
			activateHSRProblems();
			loadPictures();
		}

		private void loadPictures() {
			IPath IMG_OBJS_PATH = new Path(IMG_OBJS_CORRECTION_REMOVE_PATH);
			IMG_OBJS_CORRECTION_REMOVE = FileLocator.find(getDefault().getBundle(), IMG_OBJS_PATH, null);
			CDTSharedImages.register(IMG_OBJS_CORRECTION_REMOVE);
		}

		private void activateHSRProblems() {
			IProblem[] problems = CodanRuntime.getInstance().getCheckersRegistry().getWorkspaceProfile().getProblems();
			for (IProblem problem : problems) {
				if (!problem.getId().contains("HSR") && problem instanceof CodanProblem) { //$NON-NLS-1$
					((CodanProblem) problem).setEnabled(false);
				}
			}
		}
	}

	public static final String PLUGIN_ID = "ch.hsr.eclipse.cdt"; //$NON-NLS-1$
	public static URL IMG_OBJS_CORRECTION_REMOVE;

	private static Activator plugin;
	private static final String IMG_OBJS_CORRECTION_REMOVE_PATH = "icons/obj16/remove_correction.gif"; //$NON-NLS-1$

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		context.addBundleListener(new ActivationListener());
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

	public ITextSelection getEditorSelection() {
		return (ITextSelection) getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getSelectionProvider().getSelection();
	}
}
