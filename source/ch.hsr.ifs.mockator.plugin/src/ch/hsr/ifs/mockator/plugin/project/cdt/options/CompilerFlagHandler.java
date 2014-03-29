package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;

public class CompilerFlagHandler extends AbstractOptionsHandler {

  public CompilerFlagHandler(IProject project) {
    super(project);
  }

  public void addCompilerFlag(String flag) {
    toggleCompilerFlag(new CompilerFlagAdder(), flag);
  }

  public void removeCompilerFlag(String flag) {
    toggleCompilerFlag(new CompilerFlagRemover(), flag);
  }

  public String getCompilerFlags() {
    for (ITool optTool : getToolToAnanalyze()) {
      try {
        return optTool.getToolCommandFlagsString(null, null);
      } catch (BuildException e) {
        return null;
      }
    }
    return null;
  }

  private void toggleCompilerFlag(final F2<String, String, String> compilerFlagOp, final String flag) {
    withEveryTool(new F2<ITool, IConfiguration, Void>() {
      @Override
      public Void apply(ITool tool, IConfiguration config) {
        try {
          IOption flagsOption =
              tool.getOptionBySuperClassId(projectVariables.getCppCompilerOtherFlagsId());
          String flags = flagsOption.getStringValue();
          String newFlags = compilerFlagOp.apply(flag, flags);
          setAndSaveOption(config, tool, flagsOption, newFlags);
        } catch (BuildException e) {
          throw new MockatorException(e);
        }

        return null;
      }
    });
  }

  private static class CompilerFlagRemover implements F2<String, String, String> {
    @Override
    public String apply(String flagToRemove, String flags) {
      return flags.replaceAll(Pattern.quote(flagToRemove), "");
    }
  }

  private static class CompilerFlagAdder implements F2<String, String, String> {
    @Override
    public String apply(String flagToAdd, String flags) {
      if (!flags.contains(flagToAdd))
        return flags + " " + flagToAdd;
      return flags;
    }
  }

  @Override
  protected boolean isRequestedTool(ITool tool) {
    return isCppCompiler(tool);
  }
}
