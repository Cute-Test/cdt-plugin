package ch.hsr.ifs.mockator.plugin.linker;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtManagedProjectType;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorNature;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;


public class ReferencingExecutableFinder {

   private final IProject project;

   public ReferencingExecutableFinder(IProject project) {
      this.project = project;
   }

   public Collection<IProject> findReferencingExecutables() {
      return getReferencingProjectsWithProperty(new F1<IProject, Boolean>() {

         @Override
         public Boolean apply(IProject proj) {
            return isExecutableArtifactType(proj);
         }
      });
   }

   public Collection<IProject> findReferencingMockatorExecutables() {
      return getReferencingProjectsWithProperty(new F1<IProject, Boolean>() {

         @Override
         public Boolean apply(IProject proj) {
            return isExecutableArtifactType(proj) && isMockatorProject(proj);
         }
      });
   }

   private Collection<IProject> getReferencingProjectsWithProperty(F1<IProject, Boolean> criteria) {
      return filter(project.getReferencingProjects(), criteria);
   }

   private static boolean isMockatorProject(IProject project) {
      return new NatureHandler(project).hasNature(MockatorNature.NATURE_ID);
   }

   private static boolean isExecutableArtifactType(IProject project) {
      return CdtManagedProjectType.fromProject(project) == CdtManagedProjectType.Executable;
   }
}
