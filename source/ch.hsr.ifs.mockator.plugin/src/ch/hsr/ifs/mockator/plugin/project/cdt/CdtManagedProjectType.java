package ch.hsr.ifs.mockator.plugin.project.cdt;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;

public enum CdtManagedProjectType {
  SharedLib, StaticLib, Executable;

  public static CdtManagedProjectType fromProject(IProject project) {
    String arteFactId = getArtefactId(project);

    if (arteFactId.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB))
      return SharedLib;
    else if (arteFactId.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB))
      return StaticLib;
    else if (arteFactId.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE))
      return Executable;

    throw new MockatorException("Project type not supported");
  }

  private static String getArtefactId(IProject project) {
    return getDefaultConfiguration(project).getBuildArtefactType().getId();
  }

  private static IConfiguration getDefaultConfiguration(IProject project) {
    IManagedBuildInfo info = CdtHelper.getManagedBuildInfo(project);
    return info.getDefaultConfiguration();
  }
}
