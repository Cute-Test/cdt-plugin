package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.checkedCast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper;


public class RunConfigEnvManager {

   private final IProject                        targetProject;
   private final Optional<PreloadRunStrategy>    strategy;
   private static final ILaunchConfigurationType CUTE_LAUNCH;
   private static final ILaunchConfigurationType CDT_APP_LAUNCH;

   static {
      CUTE_LAUNCH = getManager().getLaunchConfigurationType("ch.hsr.ifs.cutelauncher.launchConfig");
      CDT_APP_LAUNCH = getManager().getLaunchConfigurationType("org.eclipse.cdt.launch.applicationLaunchType");
   }

   public RunConfigEnvManager(final IProject targetProject, final IProject sharedLibProj) {
      this.targetProject = targetProject;
      strategy = new PreloadRunFactory().getRunConfig(sharedLibProj);
   }

   public boolean hasPreloadLaunchConfig(final String sharedLibPath) {
      if (strategy.isPresent()) {
         try {
            for (final ILaunchConfiguration config : getLaunchConfigs()) {
               final Map<String, String> envVariables = getEnvVars(config.getWorkingCopy());

               if (strategy.get().hasPreloadConfig(sharedLibPath, envVariables)) {
                  return true;
               }
            }
         } catch (final CoreException e) {}
      }
      return false;
   }

   public void addPreloadLaunchConfig(final String sharedLibPath) {
      strategy.ifPresent((strat) -> {
         try {
            for (final ILaunchConfiguration config : getLaunchConfigs()) {
               final ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
               final Map<String, String> envVariables = getEnvVars(wc);
               strat.addPreloadConfig(sharedLibPath, envVariables);
               saveEnvVars(wc, envVariables);
            }
         } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
         }
      });
   }

   private static void saveEnvVars(final ILaunchConfigurationWorkingCopy wc, final Map<String, String> env) throws CoreException {
      wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);
      wc.doSave();
   }

   private static Map<String, String> getEnvVars(final ILaunchConfigurationWorkingCopy wc) throws CoreException {
      return checkedCast(wc.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, CollectionHelper.<String, String>orderPreservingMap()),
               String.class);
   }

   public void removePreloadLaunchConfig(final String sharedLibPath) {
      strategy.ifPresent((strat) -> {
         try {
            for (final ILaunchConfiguration config : getLaunchConfigs()) {
               final ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
               final Map<String, String> envVariables = getEnvVars(wc);
               strat.removePreloadConfig(sharedLibPath, envVariables);
               saveEnvVars(wc, envVariables);
            }
         } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
         }
      });
   }

   private Collection<ILaunchConfiguration> getLaunchConfigs() throws CoreException {
      return Arrays.asList(getManager().getLaunchConfigurations()).stream().filter((config) -> {
         try {
            final String typeId = config.getType().getIdentifier();
            return isCuteOrCdtExecutable(typeId) && matchesProject(config);
         } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
         }
      }).collect(Collectors.toList());
   }

   private static boolean isCuteOrCdtExecutable(final String typeId) {
      return typeId.equals(CUTE_LAUNCH.getIdentifier()) || typeId.equals(CDT_APP_LAUNCH.getIdentifier());
   }

   private boolean matchesProject(final ILaunchConfiguration launchConfig) throws CoreException {
      final ICProject cProject = CDebugUtils.getCProject(launchConfig);

      if (cProject == null) {
         return false;
      }

      return targetProject.equals(cProject.getProject());
   }

   private static ILaunchManager getManager() {
      return DebugPlugin.getDefault().getLaunchManager();
   }
}
