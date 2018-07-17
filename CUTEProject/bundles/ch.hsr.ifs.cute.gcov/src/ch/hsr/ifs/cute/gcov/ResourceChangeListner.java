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

   @Override
   public void resourceChanged(IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      IResourceDelta[] deltas = new IResourceDelta[] { delta };
      processDeltas(deltas);
   }

   protected void processDeltas(IResourceDelta[] deltas) {
      if (deltas.length == 0) return;
      for (IResourceDelta iResourceDelta : deltas) {
         try {
            IPath path = iResourceDelta.getFullPath();
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            CoverageModel CovModel = GcovPlugin.getDefault().getcModel();
            File mF = CovModel.getModelForFile(file);
            int flags = iResourceDelta.getFlags();
            if ((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.REPLACED) != 0) {
               if (mF != null) {
                  CovModel.removeFileFromModel(file);
               }
               new DeleteMarkerJob(file).schedule();
            }
         } catch (Exception e) {}
         processDeltas(iResourceDelta.getAffectedChildren());
      }
   }
}
