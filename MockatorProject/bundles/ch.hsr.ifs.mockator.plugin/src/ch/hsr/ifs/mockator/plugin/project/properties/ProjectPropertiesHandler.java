package ch.hsr.ifs.mockator.plugin.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


class ProjectPropertiesHandler {

   private final IProject project;

   public ProjectPropertiesHandler(final IProject project) {
      this.project = project;
   }

   public void setProjectProperty(final QualifiedName qfName, final String value) {
      try {
         project.setPersistentProperty(qfName, value);
      } catch (final CoreException e) {
         throw new ILTISException("Not able to store property for " + qfName.toString(), e).rethrowUnchecked();
      }
   }

   public String getProjectProperty(final QualifiedName qfName) {
      try {
         return project.getPersistentProperty(qfName);
      } catch (final CoreException e) {
         throw new ILTISException("Not able to determine property for " + qfName.toString(), e).rethrowUnchecked();
      }
   }
}
