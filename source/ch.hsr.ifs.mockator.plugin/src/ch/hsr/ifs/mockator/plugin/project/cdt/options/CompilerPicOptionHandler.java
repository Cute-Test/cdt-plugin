package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.functional.F2;

public class CompilerPicOptionHandler extends AbstractOptionsHandler {

  public CompilerPicOptionHandler(IProject project) {
    super(project);
  }

  public void setPositionIndependentCode() {
    withEveryTool(new F2<ITool, IConfiguration, Void>() {
      @Override
      public Void apply(ITool tool, IConfiguration config) {
        for (IOption option : tool.getOptions()) {
          if (isPicOption(option)) {
            setAndSaveOption(config, tool, option, true);
          }
        }
        return null;
      }
    });
  }

  private boolean isPicOption(IOption option) {
    return option.getId().equals(projectVariables.getCompilerPicId());
  }

  @Override
  protected boolean isRequestedTool(ITool tool) {
    return isCppCompiler(tool);
  }
}
