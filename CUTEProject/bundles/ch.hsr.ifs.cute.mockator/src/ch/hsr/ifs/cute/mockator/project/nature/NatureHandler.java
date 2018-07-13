package ch.hsr.ifs.cute.mockator.project.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ch.hsr.ifs.iltis.core.core.arrays.ArrayUtil;
import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


public class NatureHandler {

   private final IProject project;

   public NatureHandler(final IProject project) {
      ILTISException.Unless.isTrue("Only existing and open projects are supported", project.exists() && project.isOpen());
      this.project = project;
   }

   public void addNature(final String natureId, final IProgressMonitor pm) throws CoreException {
      if (hasNature(natureId)) return;

      final IProjectDescription desc = getProjectDescription();
      final String[] newNatures = ArrayUtil.append(desc.getNatureIds(), natureId);
      validateNewNatures(newNatures);
      desc.setNatureIds(newNatures);
      project.setDescription(desc, pm);
   }

   private IProjectDescription getProjectDescription() throws CoreException {
      return project.getDescription();
   }

   private static void validateNewNatures(final String[] newNatures) {
      final IStatus status = ResourcesPlugin.getWorkspace().validateNatureSet(newNatures);
      ILTISException.Unless.isTrue(status.getMessage(), status.getCode() == IStatus.OK);
   }

   public void removeNature(final String natureId, final IProgressMonitor pm) throws CoreException {
      final IProjectDescription description = getProjectDescription();
      description.setNatureIds(ArrayUtil.removeAndTrim(description.getNatureIds(), natureId));
      project.setDescription(description, pm);
   }

   public boolean hasNature(final String natureId) {
      try {
         return project.hasNature(natureId);
      } catch (final CoreException e) {
         return false;
      }
   }
}
