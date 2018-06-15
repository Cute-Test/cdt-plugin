package ch.hsr.ifs.cute.ui.project;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.iltis.core.core.arrays.ArrayUtil;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.GetOptionsStrategy;
import ch.hsr.ifs.cute.ui.IIncludeStrategyProvider;
import ch.hsr.ifs.cute.ui.IncludePathStrategy;
import ch.hsr.ifs.cute.ui.ProjectTools;


/**
 * @author Emanuel Graf, Tobias Stauber
 *
 */
public class CuteNature implements IProjectNature, IIncludeStrategyProvider {

   public static final String NATURE_ID = CuteUIPlugin.PLUGIN_ID + ".cutenature";

   private IProject project;

   public static void addCuteNature(IProject project, IProgressMonitor mon) throws CoreException {
      addNature(project, NATURE_ID, mon);
   }

   public static void removeCuteNature(IProject project, IProgressMonitor mon) throws CoreException {
      removeNature(project, NATURE_ID, mon);
   }

   public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      if (ArrayUtil.contains(prevNatures, natureId) == -1) {
         description.setNatureIds(ArrayUtil.append(prevNatures, natureId));
         project.setDescription(description, monitor);
      }
   }

   public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
      IProjectDescription description = project.getDescription();
      description.setNatureIds(ArrayUtil.removeAndTrim(description.getNatureIds(), natureId));
      project.setDescription(description, monitor);
   }

   @Override
   public void configure() throws CoreException {
      if (ManagedBuildManager.getBuildInfo(project) != null) {
         ICuteHeaders defaultHeaders = ICuteHeaders.getDefaultHeaders(CPPVersion.getForProject(getProject()));
         createCuteProjectFolders(project, defaultHeaders);
      }
   }

   @Override
   public void deconfigure() throws CoreException {
      removeCuteProjectFolders(project);
   }

   @Override
   public IProject getProject() {
      return project;
   }

   @Override
   public void setProject(IProject project) {
      this.project = project;
   }

   private void createCuteProjectFolders(IProject project, ICuteHeaders headers) throws CoreException {
      IFolder cuteFolder = ProjectTools.createFolder(project, "cute", true);
      headers.copyHeaderFiles(cuteFolder, new NullProgressMonitor());
      ProjectTools.setIncludePaths(replaceProjectLocation(cuteFolder), project, this);
   }

   private void removeCuteProjectFolders(IProject project) throws CoreException {
      IFolder cuteFolder = project.getFolder("cute");
      ProjectTools.removeIncludePaths(replaceProjectLocation(cuteFolder), project, this);
      ICuteHeaders.removeHeaderFiles(cuteFolder, new NullProgressMonitor());
   }

   private IPath replaceProjectLocation(IFolder cuteFolder) {
      return new Path("/${ProjName}").append(cuteFolder.getProjectRelativePath());
   }

   @Override
   public GetOptionsStrategy getStrategy(int optionType) {
      switch (optionType) {
      case IOption.INCLUDE_PATH:
         return new IncludePathStrategy();
      default:
         throw new IllegalArgumentException("Illegal Argument: " + optionType);
      }
   }
}
