/*******************************************************************************
 * Copyright (c) 2007-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import ch.hsr.ifs.testframework.launch.ILaunchObserver;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class LaunchObserver implements ILaunchObserver {

   @Override
   public void notifyBeforeLaunch(IProject project) throws CoreException {
      cleanGcdaFilesInRefedProjects(project);
   }

   private void cleanGcdaFilesInRefedProjects(IProject project) throws CoreException {
      cleanGcdaFilesinProject(project);
      for (IProject refProj : project.getReferencedProjects()) {
         cleanGcdaFilesinProject(refProj);
      }
   }

   private void cleanGcdaFilesinProject(IProject refProj) throws CoreException {
      refProj.accept(resource -> {
         if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            String fileExtension = file.getFileExtension();
            if (fileExtension != null && fileExtension.equals("gcda")) {
               file.delete(true, new NullProgressMonitor());
            }
         }
         return true;
      });
   }

   @Override
   public void notifyTermination(final IProject project) throws CoreException {
      Job gcovUpdateJob = new GcovUpdateJob("Running GCov", project);
      gcovUpdateJob.schedule();
   }

   @Override
   public void notifyAfterLaunch(IProject project) throws CoreException {}
}
