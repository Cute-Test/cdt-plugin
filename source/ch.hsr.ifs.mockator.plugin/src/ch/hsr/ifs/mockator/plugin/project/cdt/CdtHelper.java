package ch.hsr.ifs.mockator.plugin.project.cdt;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;

public abstract class CdtHelper {

  public static ITool getSuperTool(ITool tool) {
    ITool currentTool = tool;

    while (currentTool.getSuperClass() != null) {
      currentTool = currentTool.getSuperClass();
    }

    return currentTool;
  }

  public static IManagedBuildInfo getManagedBuildInfo(IProject proj) {
    IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
    Assert.notNull(info,
        String.format("Project '%s' does not have managed build information", proj.getName()));
    return info;
  }

  public static void setAndSaveOption(IProject proj, IConfiguration conf, ITool tool,
      IOption option, String newFlags) {
    ManagedBuildManager.setOption(conf, tool, option, newFlags);
    saveBuildInfo(proj);
  }

  public static void setAndSaveOption(IProject proj, IConfiguration conf, ITool tool,
      IOption option, Collection<String> values) {
    ManagedBuildManager.setOption(conf, tool, option, values.toArray(new String[values.size()]));
    saveBuildInfo(proj);
  }

  public static void setAndSaveOption(IProject proj, IConfiguration conf, ITool tool,
      IOption option, boolean active) {
    ManagedBuildManager.setOption(conf, tool, option, active);
    saveBuildInfo(proj);
  }

  private static void saveBuildInfo(IProject proj) {
    ManagedBuildManager.saveBuildInfo(proj, true);
  }
}
