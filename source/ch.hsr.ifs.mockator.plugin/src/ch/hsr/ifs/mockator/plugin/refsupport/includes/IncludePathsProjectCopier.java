package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import java.util.Collection;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.iltis.cpp.resources.CPPResourceHelper;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludePathHandler;


public class IncludePathsProjectCopier {

   private final ICProject sourceProject;

   public IncludePathsProjectCopier(ICProject sourceProject) {
      this.sourceProject = sourceProject;
   }

   public void addIncludePaths(IProject targetProject) {
      addProjectIncludePaths(targetProject);
      addSourcePathsAsIncludes(targetProject);
   }

   private void addProjectIncludePaths(IProject targetProject) {
      IncludePathHandler includePathHandler = new IncludePathHandler(targetProject);

      for (String include : getSourceProjectIncludes()) {
         IFolder pathToAdd = getFolder(include);
         includePathHandler.addInclude(pathToAdd);
      }

      includePathHandler.addInclude(sourceProject.getProject());
   }

   private static IFolder getFolder(String include) {
      return CPPResourceHelper.getWorkspaceRoot().getFolder(new Path(include));
   }

   private Collection<String> getSourceProjectIncludes() {
      return new IncludePathHandler(sourceProject.getProject()).getAllIncludes();
   }

   private void addSourcePathsAsIncludes(IProject targetProject) {
      IncludePathHandler includePathHandler = new IncludePathHandler(targetProject);

      try {
         for (ISourceRoot sr : sourceProject.getSourceRoots()) {
            if (sr.getPath().segmentCount() > 1) {
               IFolder include = CPPResourceHelper.getWorkspaceRoot().getFolder(sr.getPath());
               includePathHandler.addInclude(include);
            }
         }
      }
      catch (CModelException e) {
         throw new MockatorException(e);
      }
   }
}
