package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;

public class LinkerOptionHandler extends AbstractOptionsHandler {

  public LinkerOptionHandler(IProject project) {
    super(project);
  }

  public void removeLinkerFlag(String flagName) {
    toggleLinkerFlag(new LinkerOtherFlagRemover(), flagName);
  }

  public void addLinkerFlag(String flagName) {
    toggleLinkerFlag(new LinkerOtherFlagAdder(), flagName);
  }

  public boolean hasLinkerFlag(String flagName) {
    for (ITool optTool : getToolToAnanalyze()) {
      IOption flagsOption = optTool.getOptionBySuperClassId(projectVariables.getLinkerOtherFlags());

      if (flagsOption == null)
        return false;

      Collection<String> currentFlags = getListValues(flagsOption);

      if (currentFlags.contains(flagName))
        return true;
    }

    return false;
  }

  private void toggleLinkerFlag(final F2<String, Collection<String>, Void> linkerFlagOp,
      final String flagName) {
    withEveryTool(new F2<ITool, IConfiguration, Void>() {
      @Override
      public Void apply(ITool tool, IConfiguration config) {
        IOption flagsOption = tool.getOptionBySuperClassId(projectVariables.getLinkerOtherFlags());
        Collection<String> linkerOptions = getListValues(flagsOption);
        linkerFlagOp.apply(flagName, linkerOptions);
        setAndSaveOption(config, tool, flagsOption, linkerOptions);
        return null;
      }
    });
  }

  private static Collection<String> getListValues(IOption option) {
    try {
      return orderPreservingSet(option.getStringListValue());
    } catch (BuildException e) {
      throw new MockatorException(e);
    }
  }

  @Override
  protected boolean isRequestedTool(ITool tool) {
    return isLinker(tool);
  }

  private static class LinkerOtherFlagAdder implements F2<String, Collection<String>, Void> {
    @Override
    public Void apply(String wrapFunName, Collection<String> flags) {
      flags.add(wrapFunName);
      return null;
    }
  }

  private static class LinkerOtherFlagRemover implements F2<String, Collection<String>, Void> {
    @Override
    public Void apply(String wrapFunName, Collection<String> flags) {
      flags.remove(wrapFunName);
      return null;
    }
  }
}
