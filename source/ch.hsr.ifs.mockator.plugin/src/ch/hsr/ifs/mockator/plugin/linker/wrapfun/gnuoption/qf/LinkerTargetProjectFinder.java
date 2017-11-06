package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;


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
