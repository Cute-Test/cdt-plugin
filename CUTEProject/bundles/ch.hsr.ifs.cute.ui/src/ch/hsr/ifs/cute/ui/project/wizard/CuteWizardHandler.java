/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.GetOptionsStrategy;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.IIncludeStrategyProvider;
import ch.hsr.ifs.cute.ui.IncludePathStrategy;
import ch.hsr.ifs.cute.ui.ProjectTools;
import ch.hsr.ifs.cute.ui.project.CuteNature;


/**
 * @author Emanuel Graf
 *
 */
public class CuteWizardHandler extends MBSWizardHandler implements IIncludeStrategyProvider {

   private NewCuteProjectWizardPage cuteWizardPage;

   public CuteWizardHandler(Composite p, IWizard w) {
      super(new CuteBuildPropertyValue(), p, w);
      cuteWizardPage = initPage();
      cuteWizardPage.setWizard(w);

      MBSCustomPageManager.init();
      MBSCustomPageManager.addStockPage(cuteWizardPage, cuteWizardPage.getPageID());
   }

   protected NewCuteProjectWizardPage initPage() {
      return new NewCuteProjectWizardPage(getConfigPage(), getNewProjectCreationPage(), getWizardContainer(getWizard()));
   }

   protected IWizardContainer getWizardContainer(IWizard w) {
      return w == null ? null : w.getContainer();
   }

   @Override
   public IWizardPage getSpecificPage() {
      return cuteWizardPage;
   }

   @Override
   public void postProcess(IProject newProject, boolean created) {
      if (created) {
         doTemplatesPostProcess(newProject);
         doCustom(newProject);
      }
   }

   @Override
   protected void doCustom(final IProject newProject) {
      super.doCustom(newProject);
      IRunnableWithProgress op = monitor -> createCuteProjectSettings(newProject, monitor);
      try {
         getWizard().getContainer().run(false, true, op);
      } catch (InvocationTargetException e) {
         CuteUIPlugin.log(e);
      } catch (InterruptedException e) {
         CuteUIPlugin.log(e);
      }
   }

   protected void createCuteProjectSettings(IProject newProject, IProgressMonitor pm) {
      try {
         createCuteProject(newProject, pm);
         if (cuteWizardPage.isLibrarySelectionActive) {
            createLibSettings(newProject);
         }
      } catch (CoreException e) {
         CuteUIPlugin.log("Exception while creating cute project settings for project " + newProject.getName(), e);
      }
   }

   protected void createCuteProject(IProject project, IProgressMonitor pm) throws CoreException {
      CuteNature.addCuteNature(project, new NullProgressMonitor());
      ICuteHeaders.setForProject(project, getCuteVersion());
      createCuteProjectFolders(project);
      callAdditionalHandlers(project, pm);
      ManagedBuildManager.saveBuildInfo(project, true);
   }

   private void callAdditionalHandlers(IProject project, IProgressMonitor pm) throws CoreException {
      List<ICuteWizardAddition> adds = getAdditions();
      SubMonitor mon = SubMonitor.convert(pm, adds.size());
      for (ICuteWizardAddition addition : adds) {
         addition.getHandler().configureProject(project, mon);
         mon.worked(1);
      }
      mon.done();
   }

   protected List<ICuteWizardAddition> getAdditions() {
      return cuteWizardPage.getAdditions();
   }

   private void createCuteProjectFolders(IProject project) throws CoreException {
      ICuteHeaders cuteVersion = getCuteVersion();
      IFolder srcFolder = ProjectTools.createFolder(project, "src", false);
      copyExampleTestFiles(srcFolder, cuteVersion);
      IFile srcFile = project.getFile("src/Test.cpp");
      IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), srcFile, true);
   }

   protected void copyExampleTestFiles(IFolder srcFolder, ICuteHeaders cuteVersion) throws CoreException {
      cuteVersion.copyExampleTestFiles(srcFolder, new NullProgressMonitor());
   }

   private void createLibSettings(IProject project) throws CoreException {
      List<IProject> projects = cuteWizardPage.getCheckedProjects();
      for (IProject libProject : projects) {
         for (ICuteWizardAddition addition : getAdditions()) {
            addition.getHandler().configureLibProject(libProject);
         }
         setToolChainIncludePath(project, libProject);
      }
      ManagedBuildManager.saveBuildInfo(project, true);
      setProjectReference(project, projects);
   }

   private void setProjectReference(IProject project, List<IProject> projects) throws CoreException {
      if (!projects.isEmpty()) {
         ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, true);
         ICConfigurationDescription cfgs[] = des.getConfigurations();
         for (ICConfigurationDescription config : cfgs) {
            Map<String, String> refMap = config.getReferenceInfo();
            for (IProject refProject : projects) {
               refMap.put(refProject.getName(), "");
            }
            config.setReferenceInfo(refMap);
         }
         CCorePlugin.getDefault().setProjectDescription(project, des);
      }
   }

   private void setToolChainIncludePath(IProject project, IProject libProject) throws CoreException {
      IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
      IConfiguration config = info.getDefaultConfiguration();
      IConfiguration[] configs = info.getManagedProject().getConfigurations();
      ICSourceEntry[] sources = config.getSourceEntries();
      for (ICSourceEntry sourceEntry : sources) {
         IPath location = sourceEntry.getFullPath();
         if (location.segmentCount() == 0) {
            ProjectTools.setIncludePaths(libProject.getFullPath(), project, this);
         } else {
            ProjectTools.setIncludePaths(libProject.getFolder(location).getFullPath(), project, this);
         }
      }
      for (IConfiguration configuration : configs) {
         ICOutputEntry[] dirs = configuration.getBuildData().getOutputDirectories();
         for (ICOutputEntry outputEntry : dirs) {
            IPath location = outputEntry.getFullPath();
            if (location.segmentCount() == 0) {
               setLibraryPaths(libProject.getFullPath(), project, configuration);
            } else {
               setLibraryPaths(libProject.getFolder(location).getFullPath(), project, configuration);
            }
         }
      }
      String artifactName = config.getArtifactName();
      if (artifactName.equalsIgnoreCase("${ProjName}")) {
         setLibName(libProject.getName(), project);
      } else {
         setLibName(artifactName, project);
      }
   }

   protected void setLibraryPaths(IPath libFolder, IProject project, IConfiguration configuration) throws CoreException {
      String path = "\"${workspace_loc:" + libFolder.toPortableString() + "}\"";
      IConfiguration targetConfig = findSameConfig(configuration, project);
      try {
         IToolChain toolChain = targetConfig.getToolChain();
         ProjectTools.setOptionInConfig(path, targetConfig, toolChain.getOptions(), toolChain, IOption.LIBRARY_PATHS, this);
         ITool[] tools = targetConfig.getTools();
         for (ITool tool : tools) {
            ProjectTools.setOptionInConfig(path, targetConfig, tool.getOptions(), tool, IOption.LIBRARY_PATHS, this);
         }
      } catch (BuildException be) {
         throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, be.getMessage(), be));
      }
   }

   private IConfiguration findSameConfig(IConfiguration configuration, IResource project) {
      IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
      IConfiguration[] configs = info.getManagedProject().getConfigurations();
      for (IConfiguration iConfiguration : configs) {
         if (iConfiguration.getName().equals(configuration.getName())) { return iConfiguration; }
      }
      return info.getDefaultConfiguration();
   }

   protected void setLibName(String libName, IProject project) throws CoreException {
      ProjectTools.setOptionInAllConfigs(project, libName, IOption.LIBRARIES, this);
   }

   @Override
   public GetOptionsStrategy getStrategy(int optionType) {
      switch (optionType) {
      case IOption.LIBRARY_PATHS:
         return new LibraryPathsStrategy();
      case IOption.LIBRARIES:
         return new LibrariesStrategy();
      case IOption.INCLUDE_PATH:
         return new IncludePathStrategy();
      default:
         throw new IllegalArgumentException("Illegal Argument: " + optionType);
      }
   }

   @Override
   public boolean canFinish() {
      if (cuteWizardPage.isLibrarySelectionActive && cuteWizardPage.getCheckedProjects().isEmpty()) { return false; }
      return cuteWizardPage.isCustomPageComplete();
   }

   private static class LibraryPathsStrategy implements GetOptionsStrategy {

      @Override
      public String[] getValues(IOption option) throws BuildException {
         return option.getBasicStringListValue();
      }
   }

   private static class LibrariesStrategy implements GetOptionsStrategy {

      @Override
      public String[] getValues(IOption option) throws BuildException {
         return option.getLibraries();
      }
   }

   private ICuteHeaders getCuteVersion() {
      return ICuteHeaders.loadHeadersForVersionNumber(cuteWizardPage.getCuteVersionString());
   }
}
