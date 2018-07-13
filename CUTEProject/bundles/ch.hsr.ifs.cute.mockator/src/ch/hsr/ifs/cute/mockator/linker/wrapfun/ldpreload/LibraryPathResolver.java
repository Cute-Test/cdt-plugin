package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.mockator.project.cdt.CdtHelper;


class LibraryPathResolver {

   private final IProject libProject;

   public LibraryPathResolver(final IProject libProject) {
      this.libProject = libProject;
   }

   public String getLibraryWorkspacePath() {
      final IConfiguration defaultConfig = getDefaultConfig();
      final String defaultConfigFolderPath = getDefaultConfigFolderPath(defaultConfig);
      final String libFileName = getLibFileName(defaultConfig);
      return String.format("${workspace_loc}%s/%s", defaultConfigFolderPath, libFileName);
   }

   private String getLibFileName(final IConfiguration defaultConfig) {
      final String artifactExt = defaultConfig.getArtifactExtension();
      final String outputPrefix = defaultConfig.getOutputPrefix(artifactExt);
      return String.format("%s%s.%s", outputPrefix, libProject.getName(), artifactExt);
   }

   private String getDefaultConfigFolderPath(final IConfiguration defaultConfig) {
      return libProject.getFolder(defaultConfig.getName()).getFullPath().toString();
   }

   private IConfiguration getDefaultConfig() {
      final IManagedBuildInfo info = CdtHelper.getManagedBuildInfo(libProject);
      return info.getDefaultConfiguration();
   }
}
