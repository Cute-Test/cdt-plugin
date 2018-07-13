package ch.hsr.ifs.mockator.plugin.project.cdt;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


public abstract class CdtHelper {

   public static ITool getSuperTool(final ITool tool) {
      ITool currentTool = tool;

      while (currentTool.getSuperClass() != null) {
         currentTool = currentTool.getSuperClass();
      }

      return currentTool;
   }

   public static IManagedBuildInfo getManagedBuildInfo(final IProject proj) {
      final IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
      ILTISException.Unless.notNull(String.format("Project '%s' does not have managed build information", proj.getName()), info);
      return info;
   }

   public static void setAndSaveOption(final IProject proj, final IConfiguration conf, final ITool tool, final IOption option,
         final String newFlags) {
      ManagedBuildManager.setOption(conf, tool, option, newFlags);
      saveBuildInfo(proj);
   }

   public static void setAndSaveOption(final IProject proj, final IConfiguration conf, final ITool tool, final IOption option,
         final Collection<String> values) {
      ManagedBuildManager.setOption(conf, tool, option, values.toArray(new String[values.size()]));
      saveBuildInfo(proj);
   }

   public static void setAndSaveOption(final IProject proj, final IConfiguration conf, final ITool tool, final IOption option, final boolean active) {
      ManagedBuildManager.setOption(conf, tool, option, active);
      saveBuildInfo(proj);
   }

   private static void saveBuildInfo(final IProject proj) {
      ManagedBuildManager.saveBuildInfo(proj, true);
   }
}
