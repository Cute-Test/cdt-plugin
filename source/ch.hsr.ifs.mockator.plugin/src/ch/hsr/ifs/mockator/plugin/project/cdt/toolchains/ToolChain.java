package ch.hsr.ifs.mockator.plugin.project.cdt.toolchains;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;


public enum ToolChain {

   GnuMacOSX {

   @Override
   public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.macosx.so";
   }

   @Override
   public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
   }
   },
   GnuLinux {

   @Override
   public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.gnu.so";
   }

   @Override
   public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
   }
   },
   ClangLinux {

   @Override
   public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.clang.so";
   }

   @Override
   public ToolChainProjectVariables getCdtProjectVariables() {
      return new ClangCdtProjectVariables();
   }
   },
   GnuCygWin {

   @Override
   public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.gnu.cygwin.so";
   }

   @Override
   public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
   }
   },
   GnuMinGw {

   @Override
   public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.gnu.mingw.so";
   }

   @Override
   public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
   }
   };

   public abstract String getSharedLibProjectType();

   public abstract ToolChainProjectVariables getCdtProjectVariables();

   public static Optional<ToolChain> fromProject(final IProject project) {
      final IConfiguration configuration = getDefaultConfiguration(project);
      final IToolChain tc = getSuperToolChain(configuration.getToolChain());
      final String tcId = tc.getId();

      if (tcId.equals("cdt.managedbuild.toolchain.gnu.base") && list(tc.getOSList()).contains("linux")) {
         return Optional.of(GnuLinux);
      } else if (tcId.equals("cdt.managedbuild.toolchain.gnu.macosx.base")) {
         return Optional.of(GnuMacOSX);
      } else if (tcId.equals("cdt.managedbuild.toolchain.gnu.cygwin.base")) {
         return Optional.of(GnuCygWin);
      } else if (tcId.equals("cdt.managedbuild.toolchain.gnu.mingw.base")) { return Optional.of(GnuMinGw); }

      return Optional.empty();
   }

   public static IToolChain getSuperToolChain(final IToolChain tc) {
      IToolChain currentTc = tc;

      while (currentTc.getSuperClass() != null) {
         currentTc = currentTc.getSuperClass();
      }

      return currentTc;
   }

   public static boolean hasLinkerForToolChain(final IProject project, final ToolChain toolChain) {
      final IConfiguration config = getDefaultConfiguration(project);
      final Collection<String> linkerToolIds = toolChain.getCdtProjectVariables().getLinkerToolIds();

      for (final ITool tool : config.getToolChain().getTools()) {
         if (linkerToolIds.contains(CdtHelper.getSuperTool(tool).getId())) { return true; }
      }

      return false;
   }

   private static IConfiguration getDefaultConfiguration(final IProject project) {
      return CdtHelper.getManagedBuildInfo(project).getDefaultConfiguration();
   }
}
