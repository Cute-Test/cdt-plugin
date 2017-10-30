package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;


class LibraryPathResolver {

   private final IProject libProject;

   public LibraryPathResolver(IProject libProject) {
      this.libProject = libProject;
   }

   public String getLibraryWorkspacePath() {
      IConfiguration defaultConfig = getDefaultConfig();
      String defaultConfigFolderPath = getDefaultConfigFolderPath(defaultConfig);
      String libFileName = getLibFileName(defaultConfig);
      return String.format("${workspace_loc}%s/%s", defaultConfigFolderPath, libFileName);
   }

   private String getLibFileName(IConfiguration defaultConfig) {
      String artifactExt = defaultConfig.getArtifactExtension();
      String outputPrefix = defaultConfig.getOutputPrefix(artifactExt);
      return String.format("%s%s.%s", outputPrefix, libProject.getName(), artifactExt);
   }

   private String getDefaultConfigFolderPath(IConfiguration defaultConfig) {
      return libProject.getFolder(defaultConfig.getName()).getFullPath().toString();
   }

   private IConfiguration getDefaultConfig() {
      IManagedBuildInfo info = CdtHelper.getManagedBuildInfo(libProject);
      return info.getDefaultConfiguration();
   }
}
