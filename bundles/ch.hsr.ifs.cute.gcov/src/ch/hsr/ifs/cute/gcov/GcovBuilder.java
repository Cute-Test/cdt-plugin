/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;


/**
 * @author Emanuel Graf IFS
 *
 */
public class GcovBuilder extends IncrementalProjectBuilder {

   public static final String BUILDER_ID = GcovPlugin.PLUGIN_ID + ".gcovBuilder";

   public GcovBuilder() {}

   @Override
   protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
      return null;
   }

   @Override
   protected void clean(IProgressMonitor monitor) throws CoreException {
      IProject project = getProject();
      project.accept(resource -> {
         if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            String fileExt = file.getFileExtension();
            if (fileExt != null && (fileExt.equals("gcno") || fileExt.equals("gcda") || fileExt.equals("gcov"))) {
               file.delete(true, new NullProgressMonitor());
            }
         }
         return true;
      });
   }
}
