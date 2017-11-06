package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.functional.functions.Function2;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.GnuCdtProjectVariables;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChainProjectVariables;

abstract class AbstractOptionsHandler {

  protected ToolChainProjectVariables projectVariables;
  private final IProject project;

  public AbstractOptionsHandler(final IProject project) {
    this.project = project;
    initProjectVariables(project);
  }

  private void initProjectVariables(final IProject project) {
    final Optional<ToolChain> tc = ToolChain.fromProject(project);

    if (tc.isPresent()) {
      projectVariables = tc.get().getCdtProjectVariables();
    } else {
      projectVariables = new GnuCdtProjectVariables(); // default
    }
  }

  protected Optional<ITool> getToolToAnanalyze() {
    for (final ITool tool : getDefaultConfiguration().getToolChain().getTools()) {
      if (isRequestedTool(tool)) {
        return Optional.of(tool);
      }
    }

    return Optional.empty();
  }

  protected abstract boolean isRequestedTool(ITool tool);

  protected void withEveryTool(final Function2<ITool, IConfiguration, Void> callBack) {
    for (final IConfiguration config : getConfigurations()) {
      for (final ITool tool : config.getToolChain().getTools()) {
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

  protected boolean isCppCompiler(final ITool tool) {
    return projectVariables.getCppCompilerToolId().equals(CdtHelper.getSuperTool(tool).getId());
  }

  protected boolean isLinker(final ITool tool) {
    return projectVariables.getLinkerToolIds().contains(CdtHelper.getSuperTool(tool).getId());
  }

  protected void setAndSaveOption(final IConfiguration config, final ITool tool, final IOption option, final String newFlags) {
    CdtHelper.setAndSaveOption(project, config, tool, option, newFlags);
  }

  protected void setAndSaveOption(final IConfiguration config, final ITool tool, final IOption option, final Collection<String> values) {
    CdtHelper.setAndSaveOption(project, config, tool, option, values);
  }

  protected void setAndSaveOption(final IConfiguration config, final ITool tool, final IOption option, final boolean active) {
    CdtHelper.setAndSaveOption(project, config, tool, option, active);
  }
}
