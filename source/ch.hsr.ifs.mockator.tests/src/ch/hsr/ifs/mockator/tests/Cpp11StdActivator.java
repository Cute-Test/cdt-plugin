package ch.hsr.ifs.mockator.tests;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;


public class Cpp11StdActivator {
  private final IProject project;

  public Cpp11StdActivator(IProject project) {
    this.project = project;
  }

  public void activateCpp11Support() {
    setMacros(project, array("-std=c++0x"));
  }

  private static void setMacros(IProject project, String[] macros) {
    setOptionInAllConfigs(project, IOption.PREPROCESSOR_SYMBOLS, macros);
    ManagedBuildManager.saveBuildInfo(project, true);
  }

  private static void setOptionInAllConfigs(IProject project, int optionType, String[] newValues) {
    IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
    IConfiguration[] configs = info.getManagedProject().getConfigurations();

    try {
      for (IConfiguration conf : configs) {
        IToolChain toolChain = conf.getToolChain();
        setOptionInConfig(conf, toolChain.getOptions(), toolChain, optionType, newValues);
        ITool[] tools = conf.getTools();

        for (ITool tool : tools) {
          if (tool.getName().equals("GCC C++ Compiler")) {
            setOptionInConfig(conf, tool.getOptions(), tool, optionType, newValues);
          }
        }
      }
    } catch (BuildException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setOptionInConfig(IConfiguration config, IOption[] options,
      IHoldsOptions optionHolder, int optionType, String[] newValues) throws BuildException {
    for (IOption each : options)
      if (each.getValueType() == optionType) {
        ManagedBuildManager.setOption(config, optionHolder, each, newValues);
      }
  }
}
