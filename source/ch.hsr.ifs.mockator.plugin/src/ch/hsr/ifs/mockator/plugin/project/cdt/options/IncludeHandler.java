package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import java.util.Collection;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;

abstract class IncludeHandler extends AbstractOptionsHandler {

  public IncludeHandler(IProject project) {
    super(project);
  }

  public <U extends IResource> void addInclude(U resource) {
    alterIncludesToAllConfigs(new IncludeAdder(), resource);
  }

  public <U extends IResource> void removeInclude(U resource) {
    alterIncludesToAllConfigs(new IncludeRemover(), resource);
  }

  public Collection<String> getAllIncludes() {
    for (ITool optTool : getToolToAnanalyze()) {
      for (IOption option : optTool.getOptions()) {
        try {
          if (option.getValueType() == getOptionType())
            return getOptionValues(option);
        } catch (BuildException e) {
        }
      }
    }

    throw new MockatorException("Problems determining includes");
  }

  public <U extends IResource> boolean hasInclude(U folder) {
    String includePath = getWorkspacePath(folder);

    for (ITool optTool : getToolToAnanalyze()) {
      for (IOption option : optTool.getOptions()) {
        try {
          if (option.getValueType() == getOptionType())
            return getOptionValues(option).contains(includePath);
        } catch (BuildException e) {
        }
      }
    }

    throw new MockatorException("Problems determining includes");
  }

  @Override
  protected boolean isRequestedTool(ITool tool) {
    return isCppCompiler(tool);
  }

  private <U extends IResource> void alterIncludesToAllConfigs(
      final F2<String, Set<String>, Void> includePathOp, final U folder) {
    final String includePath = getWorkspacePath(folder);
    withEveryTool(new F2<ITool, IConfiguration, Void>() {
      @Override
      public Void apply(ITool tool, IConfiguration config) {
        for (IOption option : tool.getOptions()) {
          try {
            if (option.getValueType() == getOptionType()) {
              Set<String> includePaths = getOptionValues(option);
              includePathOp.apply(includePath, includePaths);
              setAndSaveOption(config, tool, option, includePaths);
            }
          } catch (BuildException e) {
            throw new MockatorException(e);
          }
        }

        return null;
      }
    });
  }

  protected abstract int getOptionType();

  protected abstract Set<String> getOptionValues(IOption option) throws BuildException;

  private static <U extends IResource> String getWorkspacePath(U folder) {
    return ProjRelPathGenerator.getProjectRelativePath(folder);
  }

  private static class IncludeAdder implements F2<String, Set<String>, Void> {
    @Override
    public Void apply(String newIncludePath, Set<String> includePaths) {
      includePaths.add(newIncludePath);
      return null;
    }
  }

  private static class IncludeRemover implements F2<String, Set<String>, Void> {
    @Override
    public Void apply(String includePathToRemove, Set<String> includePaths) {
      includePaths.remove(includePathToRemove);
      return null;
    }
  }
}
