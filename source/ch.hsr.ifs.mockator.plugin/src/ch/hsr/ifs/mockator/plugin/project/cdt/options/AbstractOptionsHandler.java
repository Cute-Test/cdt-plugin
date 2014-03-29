package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.functional.F2;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.GnuCdtProjectVariables;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChainProjectVariables;

abstract class AbstractOptionsHandler {
  protected ToolChainProjectVariables projectVariables;
  private final IProject project;

  public AbstractOptionsHandler(IProject project) {
    this.project = project;
    initProjectVariables(project);
  }

  private void initProjectVariables(IProject project) {
    Maybe<ToolChain> tc = ToolChain.fromProject(project);

    if (tc.isSome()) {
      projectVariables = tc.get().getCdtProjectVariables();
    } else {
      projectVariables = new GnuCdtProjectVariables(); // default
    }
  }

  protected Maybe<ITool> getToolToAnanalyze() {
    for (ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
      if (isRequestedTool(tool))
        return maybe(tool);
    }

    return none();
  }

  protected abstract boolean isRequestedTool(ITool tool);

  protected void withEveryTool(F2<ITool, IConfiguration, Void> callBack) {
    for (IConfiguration config : getConfigurations()) {
      for (ITool tool : config.getToolChain().getTools()) {
        if (isRequestedTool(tool)) {
          callBack.apply(tool, config);
        }
      }
    }
  }

  protected IConfiguration[] getConfigurations() {
    return getProjectBuildInfo().getManagedProject().getConfigurations();
  }

  protected IConfiguration getDefaultConfiguration() {
    return getProjectBuildInfo().getDefaultConfiguration();
  }

  private IManagedBuildInfo getProjectBuildInfo() {
    return CdtHelper.getManagedBuildInfo(project);
  }

  protected boolean isCppCompiler(ITool tool) {
    return projectVariables.getCppCompilerToolId().equals(CdtHelper.getSuperTool(tool).getId());
  }

  protected boolean isLinker(ITool tool) {
    return projectVariables.getLinkerToolIds().contains(CdtHelper.getSuperTool(tool).getId());
  }

  protected void setAndSaveOption(IConfiguration config, ITool tool, IOption option, String newFlags) {
    CdtHelper.setAndSaveOption(project, config, tool, option, newFlags);
  }

  protected void setAndSaveOption(IConfiguration config, ITool tool, IOption option,
      Collection<String> values) {
    CdtHelper.setAndSaveOption(project, config, tool, option, values);
  }

  protected void setAndSaveOption(IConfiguration config, ITool tool, IOption option, boolean active) {
    CdtHelper.setAndSaveOption(project, config, tool, option, active);
  }
}
