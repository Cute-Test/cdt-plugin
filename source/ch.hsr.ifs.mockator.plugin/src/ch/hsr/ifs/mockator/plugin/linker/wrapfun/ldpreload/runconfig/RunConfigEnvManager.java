package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.checkedCast;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingMap;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public class RunConfigEnvManager {
  private final IProject targetProject;
  private final Maybe<PreloadRunStrategy> strategy;
  private static final ILaunchConfigurationType CUTE_LAUNCH;
  private static final ILaunchConfigurationType CDT_APP_LAUNCH;

  static {
    CUTE_LAUNCH = getManager().getLaunchConfigurationType("ch.hsr.ifs.cutelauncher.launchConfig");
    CDT_APP_LAUNCH =
        getManager().getLaunchConfigurationType("org.eclipse.cdt.launch.applicationLaunchType");
  }

  public RunConfigEnvManager(IProject targetProject, IProject sharedLibProj) {
    this.targetProject = targetProject;
    strategy = new PreloadRunFactory().getRunConfig(sharedLibProj);
  }

  public boolean hasPreloadLaunchConfig(String sharedLibPath) {
    for (PreloadRunStrategy optStrategy : strategy) {
      try {
        for (ILaunchConfiguration config : getLaunchConfigs()) {
          Map<String, String> envVariables = getEnvVars(config.getWorkingCopy());

          if (optStrategy.hasPreloadConfig(sharedLibPath, envVariables))
            return true;
        }
      } catch (CoreException e) {
      }
    }
    return false;
  }

  public void addPreloadLaunchConfig(String sharedLibPath) {
    for (PreloadRunStrategy optStrategy : strategy) {
      try {
        for (ILaunchConfiguration config : getLaunchConfigs()) {
          ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
          Map<String, String> envVariables = getEnvVars(wc);
          optStrategy.addPreloadConfig(sharedLibPath, envVariables);
          saveEnvVars(wc, envVariables);
        }
      } catch (CoreException e) {
        throw new MockatorException(e);
      }
    }
  }

  private static void saveEnvVars(ILaunchConfigurationWorkingCopy wc, Map<String, String> env)
      throws CoreException {
    wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);
    wc.doSave();
  }

  private static Map<String, String> getEnvVars(ILaunchConfigurationWorkingCopy wc)
      throws CoreException {
    return checkedCast(
        wc.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, orderPreservingMap()),
        String.class);
  }

  public void removePreloadLaunchConfig(String sharedLibPath) {
    for (PreloadRunStrategy optStrategy : strategy) {
      try {
        for (ILaunchConfiguration config : getLaunchConfigs()) {
          ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
          Map<String, String> envVariables = getEnvVars(wc);
          optStrategy.removePreloadConfig(sharedLibPath, envVariables);
          saveEnvVars(wc, envVariables);
        }
      } catch (CoreException e) {
        throw new MockatorException(e);
      }
    }
  }

  private Collection<ILaunchConfiguration> getLaunchConfigs() throws CoreException {
    return filter(getManager().getLaunchConfigurations(), new F1<ILaunchConfiguration, Boolean>() {
      @Override
      public Boolean apply(ILaunchConfiguration config) {
        try {
          String typeId = config.getType().getIdentifier();
          return isCuteOrCdtExecutable(typeId) && matchesProject(config);
        } catch (CoreException e) {
          throw new MockatorException(e);
        }
      }
    });
  }

  private static boolean isCuteOrCdtExecutable(String typeId) {
    return typeId.equals(CUTE_LAUNCH.getIdentifier())
        || typeId.equals(CDT_APP_LAUNCH.getIdentifier());
  }

  private boolean matchesProject(ILaunchConfiguration launchConfig) throws CoreException {
    ICProject cProject = CDebugUtils.getCProject(launchConfig);

    if (cProject == null)
      return false;

    return targetProject.equals(cProject.getProject());
  }

  private static ILaunchManager getManager() {
    return DebugPlugin.getDefault().getLaunchManager();
  }
}
