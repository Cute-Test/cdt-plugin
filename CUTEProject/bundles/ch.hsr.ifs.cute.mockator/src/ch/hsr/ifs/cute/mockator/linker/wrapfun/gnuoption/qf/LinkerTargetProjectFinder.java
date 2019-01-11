package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.mockator.linker.ReferencingExecutableFinder;


public class LinkerTargetProjectFinder {

    private final IProject project;

    public LinkerTargetProjectFinder(final IProject project) {
        this.project = project;
    }

    public Collection<IProject> findLinkerTargetProjects() {
        final ReferencingExecutableFinder finder = new ReferencingExecutableFinder(project);
        final Collection<IProject> executables = finder.findReferencingExecutables();

        if (executables.isEmpty()) return list(project);

        return executables;
    }
}
