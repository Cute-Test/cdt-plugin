package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.resources.WorkspaceUtil;


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
        return WorkspaceUtil.getWorkspaceRoot().getProject(projectName);
    }
}
