package ch.hsr.ifs.cute.mockator.project.cdt;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


public enum CdtManagedProjectType {
   SharedLib, StaticLib, Executable;

   private static final String UNKNOWN = "unknown";

   public static CdtManagedProjectType fromProject(final IProject project) {
      final String arteFactId = getArtefactId(project);

      if (arteFactId.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB)) {
         return SharedLib;
      } else if (arteFactId.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB)) {
         return StaticLib;
      } else if (arteFactId.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE)) { return Executable; }

      throw new ILTISException("Project type not supported").rethrowUnchecked();
   }

   private static String getArtefactId(final IProject project) {

      final IConfiguration defaultConfiguration = getDefaultConfiguration(project);
      if (defaultConfiguration != null) {
         final IBuildPropertyValue buildArtefactType = defaultConfiguration.getBuildArtefactType();
         if (buildArtefactType != null) { return buildArtefactType.getId(); }
      }
      return UNKNOWN;
   }

   private static IConfiguration getDefaultConfiguration(final IProject project) {
      final IManagedBuildInfo info = CdtHelper.getManagedBuildInfo(project);
      return info.getDefaultConfiguration();
   }
}
