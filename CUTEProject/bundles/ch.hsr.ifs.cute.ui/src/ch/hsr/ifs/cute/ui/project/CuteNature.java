/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.cute.ui.CuteUIPlugin;


/**
 * @author Emanuel Graf
 *
 */
public class CuteNature implements IProjectNature {

   public static final String CUTE_NATURE_ID = CuteUIPlugin.PLUGIN_ID + ".cutenature";

   private IProject project;

   public static void addCuteNature(IProject project, IProgressMonitor mon) throws CoreException {
      addNature(project, CUTE_NATURE_ID, mon);
   }

   public static void removeCuteNature(IProject project, IProgressMonitor mon) throws CoreException {
      removeNature(project, CUTE_NATURE_ID, mon);
   }

   public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      for (String prevNature : prevNatures) {
         if (natureId.equals(prevNature)) return;
      }
      String[] newNatures = new String[prevNatures.length + 1];
      System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
      newNatures[0] = natureId;
      description.setNatureIds(newNatures);
      project.setDescription(description, monitor);
   }

   public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      List<String> newNatures = new ArrayList<>(Arrays.asList(prevNatures));
      newNatures.remove(natureId);
      description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
      project.setDescription(description, monitor);
   }

   @Override
   public void configure() throws CoreException {}

   @Override
   public void deconfigure() throws CoreException {}

   @Override
   public IProject getProject() {
      return project;
   }

   @Override
   public void setProject(IProject project) {
      this.project = project;
   }

}
