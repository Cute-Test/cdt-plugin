package ch.hsr.ifs.cute.elevenator.operation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class ChangeIndexFlagOperation implements IVersionModificationOperation {

	@Override
	public void perform(IProject project, CPPVersion selectedVersion, boolean enabled) {

		if (!enabled) {
			return;
		}

		ICProjectDescription pDesc = CDTPropertyManager.getProjectDescription(project);
		ICConfigurationDescription[] cfgDescs = (pDesc == null) ? null : pDesc.getConfigurations();
		for (ICConfigurationDescription configDescription : cfgDescs) {
			if (configDescription instanceof ILanguageSettingsProvidersKeeper) {

				ILanguageSettingsProvider workspaceProvider = getWorkspaceProvider(project, configDescription.getId());
				if (workspaceProvider != null) {

					GCCBuiltinSpecsDetector specsDetector = getSpecsDetector(workspaceProvider);
					if (specsDetector != null) {
						changeSpecDetector(selectedVersion, specsDetector);

						ILanguageSettingsProvidersKeeper providerKeeper = (ILanguageSettingsProvidersKeeper) configDescription;
						List<ILanguageSettingsProvider> providers = providerKeeper.getLanguageSettingProviders();
						ArrayList<ILanguageSettingsProvider> newProviders = new ArrayList<ILanguageSettingsProvider>(
								providers);

						newProviders.remove(workspaceProvider);
						newProviders.add(specsDetector);
						providerKeeper.setLanguageSettingProviders(newProviders);

					}
				}
			}
		}
		CDTPropertyManager.performOk(this);
	}

	private void changeSpecDetector(CPPVersion selectedVersion, GCCBuiltinSpecsDetector specsDetector) {
		String parameterProperty = specsDetector.getProperty("parameter");

		parameterProperty = removeSubstringToNextSpace(parameterProperty, "-std=c++");
		specsDetector.setProperty("parameter",
				parameterProperty + " -std=" + selectedVersion.getCompilerVersionString());

		LanguageSettingsManager.setStoringEntriesInProjectArea(specsDetector, true);
		LanguageSettingsManager.isStoringEntriesInProjectArea(specsDetector);
	}

	private GCCBuiltinSpecsDetector getSpecsDetector(ILanguageSettingsProvider workspaceProvider) {
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		if (rawProvider instanceof ILanguageSettingsEditableProvider && !LanguageSettingsManager
				.isStoringEntriesInProjectArea((LanguageSettingsSerializableProvider) rawProvider)) {

			try {
				ILanguageSettingsEditableProvider newProvider = ((ILanguageSettingsEditableProvider) rawProvider)
						.cloneShallow();
				if (newProvider instanceof GCCBuiltinSpecsDetector) {
					GCCBuiltinSpecsDetector specsDetector = (GCCBuiltinSpecsDetector) newProvider;
					return specsDetector;
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private ILanguageSettingsProvider getWorkspaceProvider(IProject project, String configId) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration configuration = info.getManagedProject().getConfiguration(configId);
		IToolChain toolChain = configuration.getToolChain();

		String defaultLanguageSettingsProviderIdsString = toolChain.getDefaultLanguageSettingsProviderIds();
		String[] defaultLanguageSettingsProviderIds = defaultLanguageSettingsProviderIdsString.split(";");

		// TODO: This is not completly safe i assume
		if (defaultLanguageSettingsProviderIds.length < 2) {
			return null;
		}
		String languageSettingProviderName = defaultLanguageSettingsProviderIds[1];
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
				.getWorkspaceProvider(languageSettingProviderName);
		return workspaceProvider;
	}

	public static String removeSubstringToNextSpace(String original, String substringToRemove) {
		StringBuilder sb = new StringBuilder(original);
		String resultingString = original;

		int start = original.indexOf(substringToRemove);
		if (start > -1) {
			int end = original.indexOf(" ", start);
			if (end > -1) {
				// + 1 at the end to also remove the space.
				// There is still a space at the beginning and we do
				// not want two spaces
				resultingString = sb.replace(start, end + 1, "").toString();
			}
		}
		return resultingString;
	}

}
