package ch.hsr.ifs.mockator.plugin.project.nature;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public class NatureHandler {

   private final IProject project;

   public NatureHandler(final IProject project) {
      Assert.isTrue(project.exists() && project.isOpen(), "Only existing and open projects are supported");
      this.project = project;
   }

   public void addNature(final String natureId, final IProgressMonitor pm) throws CoreException {
      if (hasNature(natureId)) return;

      final IProjectDescription desc = getProjectDescription();
      final List<String> natures = list(desc.getNatureIds());
      natures.add(natureId);
      final String[] newNatures = natures.toArray(new String[natures.size()]);
      validateNewNatures(newNatures);
      desc.setNatureIds(newNatures);
      project.setDescription(desc, pm);
   }

   private IProjectDescription getProjectDescription() throws CoreException {
      return project.getDescription();
   }

   private static void validateNewNatures(final String[] newNatures) {
      final IStatus status = ResourcesPlugin.getWorkspace().validateNatureSet(newNatures);
      Assert.isTrue(status.getCode() == IStatus.OK, status.getMessage());
   }

   public void removeNature(final String natureId, final IProgressMonitor pm) throws CoreException {
      final IProjectDescription description = getProjectDescription();
      final List<String> newNatures = list(description.getNatureIds());
      newNatures.remove(natureId);
      description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
      project.setDescription(description, pm);
   }

   public boolean hasNature(final String natureId) {
      try {
         return project.hasNature(natureId);
      }
      catch (final CoreException e) {
         return false;
      }
   }
}
