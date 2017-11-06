package ch.hsr.ifs.mockator.plugin.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;


class ProjectPropertiesHandler {

   private final IProject project;

   public ProjectPropertiesHandler(final IProject project) {
      this.project = project;
   }

   public void setProjectProperty(final QualifiedName qfName, final String value) {
      try {
         project.setPersistentProperty(qfName, value);
      }
      catch (final CoreException e) {
         throw new MockatorException("Not able to store property for " + qfName.toString(), e);
      }
   }

   public String getProjectProperty(final QualifiedName qfName) {
      try {
         return project.getPersistentProperty(qfName);
      }
      catch (final CoreException e) {
         throw new MockatorException("Not able to determine property for " + qfName.toString(), e);
      }
   }
}
