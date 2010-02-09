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
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.cute.gcov.model.CoverageModel;
import ch.hsr.ifs.cute.gcov.model.File;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ResourceChangeListner implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		IResourceDelta[] deltas = new IResourceDelta[] {delta};
		processDeltas(deltas);
	}

	protected void processDeltas(IResourceDelta[] deltas) {
		if(deltas.length == 0) return;
		for (IResourceDelta iResourceDelta : deltas) {
			try {
				IPath path = iResourceDelta.getFullPath();
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				CoverageModel CovModel = GcovPlugin.getDefault().getcModel();
				File mF = CovModel.getModelForFile(file);
				int flags = iResourceDelta.getFlags();
				if((flags & IResourceDelta.CONTENT) != 0 || (flags& IResourceDelta.REPLACED) != 0) {
					if(mF != null) {
						CovModel.removeFileFromModel(file);
					}
					new DeleteMarkerJob(file).schedule();						
				}
			}catch(Exception e) {}
			processDeltas(iResourceDelta.getAffectedChildren());
		}
	}



}
