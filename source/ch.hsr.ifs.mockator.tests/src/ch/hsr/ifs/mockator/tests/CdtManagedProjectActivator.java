package ch.hsr.ifs.mockator.tests;

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

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;

@SuppressWarnings("restriction")
public class CdtManagedProjectActivator {
  private final IProject project;

  public CdtManagedProjectActivator(IProject project) {
    Assert.notNull(project, "Project to activate managed build must not be null");
    this.project = project;
  }

  public void activateManagedBuild() throws CoreException {
    ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();
    CCorePlugin.getDefault().mapCProjectOwner(project, "Test", true);
    ICProjectDescription desc = mgr.getProjectDescription(project, true);
    IManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
    String gnuExe = "cdt.managedbuild.target.gnu.exe";
    IProjectType projType = ManagedBuildManager.getExtensionProjectType(gnuExe);
    ManagedProject managedProj = new ManagedProject(project, projType);
    info.setManagedProject(managedProj);
    activateManagedBuildInConfigs(desc, projType, managedProj);
    mgr.setProjectDescription(project, desc);
  }

  private static void activateManagedBuildInConfigs(ICProjectDescription des,
      IProjectType projType, ManagedProject managedProj) throws CoreException {
    for (IConfiguration each : getConfigsIn(projType)) {
      String id = ManagedBuildManager.calculateChildId(each.getId(), null);
      IConfiguration config = new Configuration(managedProj, (Configuration) each, id, false, true);
      des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID,
          config.getConfigurationData());
    }
  }

  private static IConfiguration[] getConfigsIn(IProjectType projType) {
    String gnuExeDebug = "cdt.managedbuild.toolchain.gnu.exe.debug";
    IToolChain tc = ManagedBuildManager.getExtensionToolChain(gnuExeDebug);
    return ManagedBuildManager.getExtensionConfigurations(tc, projType);
  }
}
