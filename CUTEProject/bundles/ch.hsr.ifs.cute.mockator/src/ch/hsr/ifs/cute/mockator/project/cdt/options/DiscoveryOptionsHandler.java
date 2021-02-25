package ch.hsr.ifs.cute.mockator.project.cdt.options;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.SPACE;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


@SuppressWarnings("restriction")
public class DiscoveryOptionsHandler extends AbstractOptionsHandler {

    public DiscoveryOptionsHandler(final IProject project) {
        super(project);
    }

    public void addCpp11Support() {
        toggleCpp11Support((runArgs) -> {
            final String cpp11ExperimentalFlag = projectVariables.getCpp11ExperimentalFlag();
            return runArgs.indexOf(cpp11ExperimentalFlag) < 0 ? cpp11ExperimentalFlag + SPACE + runArgs : runArgs;
        });
    }

    public void removeCpp11Support() {
        toggleCpp11Support((runArgs) -> {
            final String p = Pattern.quote(projectVariables.getCpp11ExperimentalFlag());
            return runArgs.replaceAll(p, "").trim();
        });
    }

    private void toggleCpp11Support(final Function<String, String> runArgsHandler) {
        final ICfgScannerConfigBuilderInfo2Set scannerSet = getDiscoveryScannerConfig(getDefaultConfiguration());

        for (final CfgInfoContext context : scannerSet.getContexts()) {
            if (!isRequestedTool(context.getTool())) {
                continue;
            }

            final IScannerConfigBuilderInfo2 scannerConfig = scannerSet.getInfo(context);
            for (final String providerId : scannerConfig.getProviderIdList()) {
                try {
                    final String runArgs = scannerSet.getInfo(context).getProviderRunArguments(providerId);
                    final String modifiedRunArgs = runArgsHandler.apply(runArgs);
                    setAndSaveScannerConfig(scannerConfig, providerId, modifiedRunArgs);
                } catch (final CoreException e) {
                    throw new ILTISException(e).rethrowUnchecked();
                }
            }
        }
    }

    private static void setAndSaveScannerConfig(final IScannerConfigBuilderInfo2 scannerConfig, final String providerId, final String modifiedRunArgs)
            throws CoreException {
        scannerConfig.setProviderRunArguments(providerId, modifiedRunArgs);
        scannerConfig.save();
    }

    private static ICfgScannerConfigBuilderInfo2Set getDiscoveryScannerConfig(final IConfiguration configuration) {
        return CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(configuration);
    }

    @Override
    protected boolean isRequestedTool(final ITool tool) {
        return isCppCompiler(tool);
    }
}
