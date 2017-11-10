package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;


class UniqueProjectNameCreator {

   private final String projectName;

   public UniqueProjectNameCreator(final String projectName) {
      this.projectName = projectName;
   }

   public String getUniqueProjectName() {
      String newProjectName = projectName;

      for (int i = 1; getProject(newProjectName).exists(); ++i) {
         newProjectName = projectName + i;
      }

      return newProjectName;
   }

   private static IProject getProject(final String projectName) {
      return CProjectUtil.getWorkspaceRoot().getProject(projectName);
   }
}
