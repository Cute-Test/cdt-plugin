package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.functional.functions.Function2;

public class CompilerPicOptionHandler extends AbstractOptionsHandler {

  public CompilerPicOptionHandler(final IProject project) {
    super(project);
  }

  public void setPositionIndependentCode() {
    withEveryTool(new Function2<ITool, IConfiguration, Void>() {

      @Override
      public Void apply(final ITool tool, final IConfiguration config) {
        for (final IOption option : tool.getOptions()) {
          if (isPicOption(option)) {
            setAndSaveOption(config, tool, option, true);
          }
        }
        return null;
      }
    });
  }

  private boolean isPicOption(final IOption option) {
    return option.getId().equals(projectVariables.getCompilerPicId());
  }

  @Override
  protected boolean isRequestedTool(final ITool tool) {
    return isCppCompiler(tool);
  }
}
