package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import java.util.Collection;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.resources.ProjectUtil;

import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludePathHandler;


public class IncludePathsProjectCopier {

   private final ICProject sourceProject;

   public IncludePathsProjectCopier(final ICProject sourceProject) {
      this.sourceProject = sourceProject;
   }

   public void addIncludePaths(final IProject targetProject) {
      addProjectIncludePaths(targetProject);
      addSourcePathsAsIncludes(targetProject);
   }

   private void addProjectIncludePaths(final IProject targetProject) {
      final IncludePathHandler includePathHandler = new IncludePathHandler(targetProject);

      for (final String include : getSourceProjectIncludes()) {
         final IFolder pathToAdd = getFolder(include);
         includePathHandler.addInclude(pathToAdd);
      }

      includePathHandler.addInclude(sourceProject.getProject());
   }

   private static IFolder getFolder(final String include) {
      return ProjectUtil.getWorkspaceRoot().getFolder(new Path(include));
   }

   private Collection<String> getSourceProjectIncludes() {
      return new IncludePathHandler(sourceProject.getProject()).getAllIncludes();
   }

   private void addSourcePathsAsIncludes(final IProject targetProject) {
      final IncludePathHandler includePathHandler = new IncludePathHandler(targetProject);

      try {
         for (final ISourceRoot sr : sourceProject.getSourceRoots()) {
            if (sr.getPath().segmentCount() > 1) {
               final IFolder include = ProjectUtil.getWorkspaceRoot().getFolder(sr.getPath());
               includePathHandler.addInclude(include);
            }
         }
      } catch (final CModelException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
