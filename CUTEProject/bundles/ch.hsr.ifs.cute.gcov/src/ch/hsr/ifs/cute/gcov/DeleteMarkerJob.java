/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 *
 */
public class DeleteMarkerJob extends Job {

    private final IFile file;

    public DeleteMarkerJob(IFile file) {
        super(Messages.getString("CuteGcov.DeleteMarkerJob.deleteMarker") + file.toString());
        this.file = file;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            file.deleteMarkers(GcovPlugin.COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
            file.deleteMarkers(GcovPlugin.UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
            file.deleteMarkers(GcovPlugin.PARTIALLY_MARKER_TYPE, true, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            IStatus s = e.getStatus();
            if (s instanceof IResourceStatus) {
                IResourceStatus resStatus = (IResourceStatus) s;
                if (resStatus.getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
                    return Status.OK_STATUS;
                }
            }
            GcovPlugin.log(e);
            return new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e.getLocalizedMessage());
        }
        return Status.OK_STATUS;
    }
}
