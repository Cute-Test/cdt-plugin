package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.SPACE;

import java.util.regex.Pattern;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;

@SuppressWarnings("restriction")
public class DiscoveryOptionsHandler extends AbstractOptionsHandler {

  public DiscoveryOptionsHandler(IProject project) {
    super(project);
  }

  public void addCpp11Support() {
    toggleCpp11Support(new F1<String, String>() {
      @Override
      public String apply(String runArgs) {
        String cpp11ExperimentalFlag = projectVariables.getCpp11ExperimentalFlag();

        if (isMissingCpp11Flag(runArgs, cpp11ExperimentalFlag))
          return cpp11ExperimentalFlag + SPACE + runArgs;

        return runArgs;
      }

      private boolean isMissingCpp11Flag(String runArgs, String cpp11ExperimentalFlag) {
        return runArgs.indexOf(cpp11ExperimentalFlag) < 0;
      }
    });
  }

  public void removeCpp11Support() {
    toggleCpp11Support(new F1<String, String>() {
      @Override
      public String apply(String runArgs) {
        String p = Pattern.quote(projectVariables.getCpp11ExperimentalFlag());
        return runArgs.replaceAll(p, "").trim();
      }
    });
  }

  private void toggleCpp11Support(F1<String, String> runArgsHandler) {
    ICfgScannerConfigBuilderInfo2Set scannerSet =
        getDiscoveryScannerConfig(getDefaultConfiguration());

    for (CfgInfoContext context : scannerSet.getContexts()) {
      if (!isRequestedTool(context.getTool())) {
        continue;
      }

      IScannerConfigBuilderInfo2 scannerConfig = scannerSet.getInfo(context);
      for (String providerId : scannerConfig.getProviderIdList()) {
        try {
          String runArgs = scannerSet.getInfo(context).getProviderRunArguments(providerId);
          String modifiedRunArgs = runArgsHandler.apply(runArgs);
          setAndSaveScannerConfig(scannerConfig, providerId, modifiedRunArgs);
        } catch (CoreException e) {
          throw new MockatorException(e);
        }
      }
    }
  }

  private static void setAndSaveScannerConfig(IScannerConfigBuilderInfo2 scannerConfig,
      String providerId, String modifiedRunArgs) throws CoreException {
    scannerConfig.setProviderRunArguments(providerId, modifiedRunArgs);
    scannerConfig.save();
  }

  private static ICfgScannerConfigBuilderInfo2Set getDiscoveryScannerConfig(
      IConfiguration configuration) {
    return CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(configuration);
  }

  @Override
  protected boolean isRequestedTool(ITool tool) {
    return isCppCompiler(tool);
  }
}
