package ch.hsr.ifs.mockator.plugin.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


@SuppressWarnings("restriction")
public class CdtManagedProjectActivator {

   private final IProject project;

   public CdtManagedProjectActivator(final IProject project) {
      ILTISException.Unless.notNull(project, "Project to activate managed build must not be null");
      this.project = project;
   }

   public void activateManagedBuild() throws CoreException {
      final ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();
      CCorePlugin.getDefault().mapCProjectOwner(project, "Test", true);
      final ICProjectDescription desc = mgr.getProjectDescription(project, true);
      final IManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
      final String gnuExe = "cdt.managedbuild.target.gnu.exe";
      final IProjectType projType = ManagedBuildManager.getExtensionProjectType(gnuExe);
      final ManagedProject managedProj = new ManagedProject(project, projType);
      info.setManagedProject(managedProj);
      activateManagedBuildInConfigs(desc, projType, managedProj);
      mgr.setProjectDescription(project, desc);
   }

   private static void activateManagedBuildInConfigs(final ICProjectDescription des, final IProjectType projType, final ManagedProject managedProj)
         throws CoreException {
      for (final IConfiguration each : getConfigsIn(projType)) {
         final String id = ManagedBuildManager.calculateChildId(each.getId(), null);
         final IConfiguration config = new Configuration(managedProj, (Configuration) each, id, false, true);
         des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, config.getConfigurationData());
      }
   }

   private static IConfiguration[] getConfigsIn(final IProjectType projType) {
      final String gnuExeDebug = "cdt.managedbuild.toolchain.gnu.exe.debug";
      final IToolChain tc = ManagedBuildManager.getExtensionToolChain(gnuExeDebug);
      return ManagedBuildManager.getExtensionConfigurations(tc, projType);
   }
}
