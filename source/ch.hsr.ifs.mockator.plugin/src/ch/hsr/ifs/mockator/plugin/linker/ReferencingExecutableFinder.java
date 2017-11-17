package ch.hsr.ifs.mockator.plugin.linker;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;


public class ReferencingExecutableFinder {

   private final IProject project;

   public ReferencingExecutableFinder(final IProject project) {
      this.project = project;
   }

   public Collection<IProject> findReferencingExecutables() {
      return getReferencingProjectsWithProperty((proj) -> isExecutableArtifactType(proj));
   }

   public Collection<IProject> findReferencingMockatorExecutables() {
      return getReferencingProjectsWithProperty((proj) -> isExecutableArtifactType(proj) && isMockatorProject(proj));
   }

   private Collection<IProject> getReferencingProjectsWithProperty(final Predicate<IProject> criteria) {
      return Arrays.asList(project.getReferencingProjects()).stream().filter(criteria).collect(Collectors.toList());
   }

   private static boolean isMockatorProject(final IProject project) {
      return new NatureHandler(project).hasNature(MockatorNature.NATURE_ID);
   }

   private static boolean isExecutableArtifactType(final IProject project) {
      return CdtManagedProjectType.fromProject(project) == CdtManagedProjectType.Executable;
   }
}
