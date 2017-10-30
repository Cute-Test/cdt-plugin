package ch.hsr.ifs.mockator.plugin.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;


class ProjectPropertiesHandler {

   private final IProject project;

   public ProjectPropertiesHandler(IProject project) {
      this.project = project;
   }

   public void setProjectProperty(QualifiedName qfName, String value) {
      try {
         project.setPersistentProperty(qfName, value);
      }
      catch (CoreException e) {
         throw new MockatorException("Not able to store property for " + qfName.toString(), e);
      }
   }

   public String getProjectProperty(QualifiedName qfName) {
      try {
         return project.getPersistentProperty(qfName);
      }
      catch (CoreException e) {
         throw new MockatorException("Not able to determine property for " + qfName.toString(), e);
      }
   }
}
