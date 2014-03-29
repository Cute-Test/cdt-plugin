package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;

class UniqueProjectNameCreator {
  private final String projectName;

  public UniqueProjectNameCreator(String projectName) {
    this.projectName = projectName;
  }

  public String getUniqueProjectName() {
    String newProjectName = projectName;

    for (int i = 1; getProject(newProjectName).exists(); ++i) {
      newProjectName = projectName + i;
    }

    return newProjectName;
  }

  private static IProject getProject(String projectName) {
    return ProjectUtil.getWorkspaceRoot().getProject(projectName);
  }
}
