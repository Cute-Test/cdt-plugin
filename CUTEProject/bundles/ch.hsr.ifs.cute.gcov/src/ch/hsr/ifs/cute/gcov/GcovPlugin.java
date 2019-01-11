/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.hsr.ifs.cute.gcov.model.CoverageModel;


public class GcovPlugin extends AbstractUIPlugin {

    public static final String  PLUGIN_ID = "ch.hsr.ifs.cute.gcov";
    private static GcovPlugin   plugin;
    private final CoverageModel cModel    = new CoverageModel();

    public static final String GCOV_MARKER_TYPE      = "ch.hsr.ifs.cute.gcov.coverageMarker";
    public static final String UNCOVER_MARKER_TYPE   = "ch.hsr.ifs.cute.gcov.lineUnCoverMarker";
    public static final String COVER_MARKER_TYPE     = "ch.hsr.ifs.cute.gcov.lineCoverMarker";
    public static final String PARTIALLY_MARKER_TYPE = "ch.hsr.ifs.cute.gcov.linePartialCoverMarker";

    public GcovPlugin() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListner(), IResourceChangeEvent.POST_CHANGE);
    }

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

    public static GcovPlugin getDefault() {
        return plugin;
    }

    public CoverageModel getcModel() {
        return cModel;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static void log(String message) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e));
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
}
