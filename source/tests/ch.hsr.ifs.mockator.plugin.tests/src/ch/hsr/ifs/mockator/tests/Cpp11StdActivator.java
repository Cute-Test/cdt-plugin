package ch.hsr.ifs.mockator.tests;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.array;

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

   public Cpp11StdActivator(final IProject project) {
      this.project = project;
   }

   public void activateCpp11Support() {
      setMacros(project, array("-std=c++0x"));
   }

   private static void setMacros(final IProject project, final String[] macros) {
      setOptionInAllConfigs(project, IOption.PREPROCESSOR_SYMBOLS, macros);
      ManagedBuildManager.saveBuildInfo(project, true);
   }

   private static void setOptionInAllConfigs(final IProject project, final int optionType, final String[] newValues) {
      final IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
      final IConfiguration[] configs = info.getManagedProject().getConfigurations();

      try {
         for (final IConfiguration conf : configs) {
            final IToolChain toolChain = conf.getToolChain();
            setOptionInConfig(conf, toolChain.getOptions(), toolChain, optionType, newValues);
            final ITool[] tools = conf.getTools();

            for (final ITool tool : tools) {
               if (tool.getName().equals("GCC C++ Compiler")) {
                  setOptionInConfig(conf, tool.getOptions(), tool, optionType, newValues);
               }
            }
         }
      }
      catch (final BuildException e) {
         throw new RuntimeException(e);
      }
   }

   private static void setOptionInConfig(final IConfiguration config, final IOption[] options, final IHoldsOptions optionHolder, final int optionType,
         final String[] newValues) throws BuildException {
      for (final IOption each : options)
         if (each.getValueType() == optionType) {
            ManagedBuildManager.setOption(config, optionHolder, each, newValues);
         }
   }
}
