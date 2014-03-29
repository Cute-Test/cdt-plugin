package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;

public class LinkerTargetProjectFinder {
  private final IProject project;

  public LinkerTargetProjectFinder(IProject project) {
    this.project = project;
  }

  public Collection<IProject> findLinkerTargetProjects() {
    ReferencingExecutableFinder finder = new ReferencingExecutableFinder(project);
    Collection<IProject> executables = finder.findReferencingExecutables();

    if (executables.isEmpty())
      return list(project);

    return executables;
  }
}
