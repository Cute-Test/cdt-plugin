/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
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
 *
 */
public class DeleteMarkerJob extends Job {
	
	private IFile file;

	public DeleteMarkerJob(IFile file) {
		super(Messages.DeleteMarkerJob_deleteMarker + file.toString());
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
				if(resStatus.getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
					return Status.OK_STATUS;
				}
			}
			e.printStackTrace();
			return new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e.getLocalizedMessage());
		}
		return Status.OK_STATUS;
	}

}
