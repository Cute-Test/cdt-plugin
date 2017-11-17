package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload;

import java.util.Optional;

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

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;

import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;


class SharedLibProjectCreator {

   private final String   newProjectName;
   private final IProject projectToInheritFrom;

   public SharedLibProjectCreator(final String projectName, final IProject projectToInheritFrom) {
      newProjectName = projectName;
      this.projectToInheritFrom = projectToInheritFrom;
   }

   public IProject createSharedLib(final IProgressMonitor pm) throws CoreException {
      final IProject project = createEmptyProject(pm);
      addCdtNatures(project, pm);
      makeManagedCdtSharedLibProj(project);
      return project;
   }

   private IProject createEmptyProject(final IProgressMonitor pm) throws CoreException {
      final IProject project = CProjectUtil.getWorkspaceRoot().getProject(newProjectName);
      project.create(pm);
      project.open(pm);
      return project;
   }

   private static void addCdtNatures(final IProject project, final IProgressMonitor pm) throws CoreException {
      CProjectNature.addCNature(project, pm);
      CCProjectNature.addCCNature(project, pm);
   }

   private void makeManagedCdtSharedLibProj(final IProject project) throws CoreException {
      final ICProjectDescriptionManager mgr = CoreModel.getDefault().getProjectDescriptionManager();

      if (hasAlreadyProjectDescription(project, mgr)) { return; }

      setSharedLibProjectDesc(project, mgr);
   }

   private void setSharedLibProjectDesc(final IProject project, final ICProjectDescriptionManager mgr) throws CoreException {
      final ManagedBuildInfo mbInfo = ManagedBuildManager.createBuildInfo(project);
      final IProjectType projType = getGnuSharedLibProjType();
      final ManagedProject mProj = createManagedProject(project, mbInfo, projType);
      final ICProjectDescription projDesc = createConfigurations(project, mgr, projType, mProj);
      mgr.setProjectDescription(project, projDesc);
   }

   private ICProjectDescription createConfigurations(final IProject project, final ICProjectDescriptionManager mgr, final IProjectType projType,
         final ManagedProject mProj) throws CoreException {
      final ICProjectDescription projDesc = mgr.createProjectDescription(project, true);
      final IToolChain tc = getToolChain();

      for (final IConfiguration config : ManagedBuildManager.getExtensionConfigurations(tc, projType)) {
         if (!(config instanceof Configuration)) {
            continue;
         }

         final Configuration cf = createNewConfiguration(projDesc, (Configuration) config, mProj, project, tc);
         configureBuilder(cf);
      }

      return projDesc;
   }

   private static ManagedProject createManagedProject(final IProject project, final ManagedBuildInfo mbInfo, final IProjectType pType) {
      final ManagedProject mProject = new ManagedProject(project, pType);
      mbInfo.setManagedProject(mProject);
      return mProject;
   }

   private static void configureBuilder(final Configuration config) throws CoreException {
      final IBuilder builder = config.getEditableBuilder();
      builder.setManagedBuildOn(true);
   }

   private static Configuration createNewConfiguration(final ICProjectDescription desc, final Configuration cf, final ManagedProject managed,
         final IProject p, final IToolChain tc) throws WriteAccessException, CoreException {
      final String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
      final Configuration config = new Configuration(managed, cf, id, false, true);
      final ICConfigurationDescription cfgDes = desc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, config.getConfigurationData());
      config.setConfigurationDescription(cfgDes);
      config.exportArtifactInfo();
      config.setName(tc.getName().replaceAll("\\s", ""));
      config.setArtifactName(p.getName());
      return config;
   }

   private static boolean hasAlreadyProjectDescription(final IProject project, final ICProjectDescriptionManager mgr) {
      return mgr.getProjectDescription(project, true) != null;
   }

   private IProjectType getGnuSharedLibProjType() {
      final Optional<ToolChain> tc = ToolChain.fromProject(projectToInheritFrom);
      ILTISException.Unless.isTrue(tc.isPresent(), "Could not determine toolchain");
      final String sharedLibProjectType = tc.get().getSharedLibProjectType();
      return ManagedBuildManager.getExtensionProjectType(sharedLibProjectType);
   }

   private IConfiguration getDefaultConfig() {
      return CdtHelper.getManagedBuildInfo(projectToInheritFrom).getDefaultConfiguration();
   }

   private IToolChain getToolChain() {
      return ToolChain.getSuperToolChain(getDefaultConfig().getToolChain());
   }
}
