package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;

@SuppressWarnings("restriction")
class SharedLibProjectCreator {
  private final String newProjectName;
  private final IProject projectToInheritFrom;

  public SharedLibProjectCreator(String projectName, IProject projectToInheritFrom) {
    this.newProjectName = projectName;
    this.projectToInheritFrom = projectToInheritFrom;
  }

  public IProject createSharedLib(IProgressMonitor pm) throws CoreException {
    IProject project = createEmptyProject(pm);
    addCdtNatures(project, pm);
    makeManagedCdtSharedLibProj(project);
    return project;
  }

  private IProject createEmptyProject(IProgressMonitor pm) throws CoreException {
    IProject project = ProjectUtil.getWorkspaceRoot().getProject(newProjectName);
    project.create(pm);
    project.open(pm);
    return project;
  }

  private static void addCdtNatures(IProject project, IProgressMonitor pm) throws CoreException {
    CProjectNature.addCNature(project, pm);
    CCProjectNature.addCCNature(project, pm);
  }

  private void makeManagedCdtSharedLibProj(IProject project) throws CoreException {
    ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();

    if (hasAlreadyProjectDescription(project, mgr))
      return;

    setSharedLibProjectDesc(project, mgr);
  }

  private void setSharedLibProjectDesc(IProject project, ICProjectDescriptionManager mgr)
      throws CoreException {
    ManagedBuildInfo mbInfo = ManagedBuildManager.createBuildInfo(project);
    IProjectType projType = getGnuSharedLibProjType();
    ManagedProject mProj = createManagedProject(project, mbInfo, projType);
    ICProjectDescription projDesc = createConfigurations(project, mgr, projType, mProj);
    mgr.setProjectDescription(project, projDesc);
  }

  private ICProjectDescription createConfigurations(IProject project,
      ICProjectDescriptionManager mgr, IProjectType projType, ManagedProject mProj)
      throws CoreException {
    ICProjectDescription projDesc = mgr.createProjectDescription(project, true);
    IToolChain tc = getToolChain();

    for (IConfiguration config : ManagedBuildManager.getExtensionConfigurations(tc, projType)) {
      if (!(config instanceof Configuration)) {
        continue;
      }

      Configuration cf =
          createNewConfiguration(projDesc, (Configuration) config, mProj, project, tc);
      configureBuilder(cf);
    }

    return projDesc;
  }

  private static ManagedProject createManagedProject(IProject project, ManagedBuildInfo mbInfo,
      IProjectType pType) {
    ManagedProject mProject = new ManagedProject(project, pType);
    mbInfo.setManagedProject(mProject);
    return mProject;
  }

  private static void configureBuilder(Configuration config) throws CoreException {
    IBuilder builder = config.getEditableBuilder();
    builder.setManagedBuildOn(true);
  }

  private static Configuration createNewConfiguration(ICProjectDescription desc, Configuration cf,
      ManagedProject managed, IProject p, IToolChain tc) throws WriteAccessException, CoreException {
    String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
    Configuration config = new Configuration(managed, cf, id, false, true);
    ICConfigurationDescription cfgDes =
        desc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID,
            config.getConfigurationData());
    config.setConfigurationDescription(cfgDes);
    config.exportArtifactInfo();
    config.setName(tc.getName().replaceAll("\\s", ""));
    config.setArtifactName(p.getName());
    return config;
  }

  private static boolean hasAlreadyProjectDescription(IProject project,
      ICProjectDescriptionManager mgr) {
    return mgr.getProjectDescription(project, true) != null;
  }

  private IProjectType getGnuSharedLibProjType() {
    Maybe<ToolChain> tc = ToolChain.fromProject(projectToInheritFrom);
    Assert.isTrue(tc.isSome(), "Could not determine toolchain");
    String sharedLibProjectType = tc.get().getSharedLibProjectType();
    return ManagedBuildManager.getExtensionProjectType(sharedLibProjectType);
  }

  private IConfiguration getDefaultConfig() {
    return CdtHelper.getManagedBuildInfo(projectToInheritFrom).getDefaultConfiguration();
  }

  private IToolChain getToolChain() {
    return ToolChain.getSuperToolChain(getDefaultConfig().getToolChain());
  }
}
