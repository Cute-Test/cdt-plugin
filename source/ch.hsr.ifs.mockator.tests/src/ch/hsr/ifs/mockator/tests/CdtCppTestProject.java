package ch.hsr.ifs.mockator.tests;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.orderPreservingSet;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;


@SuppressWarnings("restriction")
public class CdtCppTestProject {

   private final IProject project;

   public static CdtCppTestProject withOpenedProject() throws CoreException {
      return new CdtCppTestProject(true);
   }

   public static CdtCppTestProject withClosedProject() throws CoreException {
      return new CdtCppTestProject(false);
   }

   private CdtCppTestProject(final boolean shouldOpen) throws CoreException {
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      project = root.getProject("Test");
      project.create(new NullProgressMonitor());

      if (shouldOpen) {
         project.open(new NullProgressMonitor());
      }
   }

   public void dispose() throws CoreException {
      project.delete(true, true, new NullProgressMonitor());
   }

   public IProject getProject() {
      return project;
   }

   public void addCppNatures() throws CoreException {
      addNatureToProject(CProjectNature.C_NATURE_ID);
      CCorePlugin.getDefault().mapCProjectOwner(project, "Test", false);
      CCorePlugin.getDefault().getCoreModel().create(project);
      addNatureToProject(CCProjectNature.CC_NATURE_ID);
   }

   private void addNatureToProject(final String natureId) throws CoreException {
      final IProjectDescription description = project.getDescription();
      final String[] prevNatures = description.getNatureIds();
      final String[] newNatures = new String[prevNatures.length + 1];
      System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
      newNatures[prevNatures.length] = natureId;
      description.setNatureIds(newNatures);
      project.setDescription(description, new NullProgressMonitor());
   }

   public void activateManagedBuild() throws CoreException {
      final CdtManagedProjectActivator activator = new CdtManagedProjectActivator(project);
      activator.activateManagedBuild();
   }

   public boolean hasIncludeForFolder(final String folder) throws BuildException {
      for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
         if (isToolCppCompiler(tool)) {
            for (final IOption option : tool.getOptions()) {
               if (option.getValueType() == IOption.INCLUDE_PATH) {
                  final String[] includePaths = option.getIncludePaths();
                  return orderPreservingSet(includePaths).contains(folder);
               }
            }
         }
      }

      throw new ILTISException("Problems determining includes").rethrowUnchecked();
   }

   public boolean hasIncludeForFile(final String filePath) throws BuildException {
      for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
         if (isToolCppCompiler(tool)) {
            for (final IOption option : tool.getOptions()) {
               if (option.getValueType() == IOption.INCLUDE_FILES) {
                  final String[] includePaths = option.getBasicStringListValue();
                  return orderPreservingSet(includePaths).contains(filePath);
               }
            }
         }
      }

      throw new ILTISException("Problems determining includes").rethrowUnchecked();
   }

   private IConfiguration getDefaultConfiguration() {
      final IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
      return info.getDefaultConfiguration();
   }

   public boolean hasCpp11DiscoveryOptionSet() {
      final ICfgScannerConfigBuilderInfo2Set scannerSet = getDiscoveryScannerConfig(getDefaultConfiguration());

      for (final CfgInfoContext context : scannerSet.getContexts()) {
         if (!isToolCppCompiler(context.getTool())) {
            continue;
         }

         final IScannerConfigBuilderInfo2 scannerConfig = scannerSet.getInfo(context);

         for (final String providerId : scannerConfig.getProviderIdList()) {
            final String runArgs = scannerSet.getInfo(context).getProviderRunArguments(providerId);

            if (runArgs.contains("-std=c++0x")) { return true; }
         }
      }

      return false;
   }

   private static ICfgScannerConfigBuilderInfo2Set getDiscoveryScannerConfig(final IConfiguration configuration) {
      return CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(configuration);
   }

   public boolean hasCompilerFlag(final String compilerFlag) throws BuildException {
      for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
         if (isToolCppCompiler(tool)) { return tool.getToolCommandFlagsString(null, null).contains(compilerFlag); }
      }

      return false;
   }

   private static boolean isToolCppCompiler(final ITool tool) {
      return "cdt.managedbuild.tool.gnu.cpp.compiler".equals(getSuperTool(tool).getId());
   }

   private static boolean isToolLinker(final ITool tool) {
      return "cdt.managedbuild.tool.gnu.cpp.linker".equals(getSuperTool(tool).getId());
   }

   private static ITool getSuperTool(final ITool tool) {
      return CdtHelper.getSuperTool(tool);
   }

   public boolean hasLinkerOption(final String linkerOption) throws BuildException {
      for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
         if (!isToolLinker(tool)) {
            continue;
         }

         final IOption flagsOption = tool.getOptionBySuperClassId("gnu.cpp.link.option.other");

         if (orderPreservingSet(flagsOption.getStringListValue()).contains(linkerOption)) { return true; }
      }

      return false;
   }

   public boolean hasMacroSet(final String macro) throws BuildException {
      for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
         if (!isToolCppCompiler(tool)) {
            continue;
         }

         final IOption option = tool.getOptionBySuperClassId("gnu.cpp.compiler.option.preprocessor.def");
         if (orderPreservingSet(option.getDefinedSymbols()).contains(macro)) { return true; }
      }

      return false;
   }

   public boolean hasLinkerLibrary(final String linkerLibrary) throws BuildException {
      for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
         if (!isToolLinker(tool)) {
            continue;
         }

         for (final IOption option : tool.getOptions()) {
            if (option.getValueType() != IOption.LIBRARIES) {
               continue;
            }

            if (orderPreservingSet(option.getLibraries()).contains(linkerLibrary)) { return true; }
         }
      }

      return false;
   }
}
