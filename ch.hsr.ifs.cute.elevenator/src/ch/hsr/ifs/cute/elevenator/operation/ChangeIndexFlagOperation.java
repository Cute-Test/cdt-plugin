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
import org.eclipse.cdt.managedbuilder.internal.language.settings.providers.GCCBuiltinSpecsDetectorMinGW;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class ChangeIndexFlagOperation implements IVersionModificationOperation {

	@Override
	public void perform(IProject project, CPPVersion selectedVersion) {

		ILanguageSettingsProvider minGWProvider = LanguageSettingsManager
				.getWorkspaceProvider("org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetectorMinGW");
		if (minGWProvider != null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(minGWProvider);
			if (rawProvider instanceof ILanguageSettingsEditableProvider && !LanguageSettingsManager
					.isStoringEntriesInProjectArea((LanguageSettingsSerializableProvider) rawProvider)) {

				try {
					ILanguageSettingsEditableProvider newProvider = ((ILanguageSettingsEditableProvider) rawProvider)
							.cloneShallow();
					if (newProvider instanceof GCCBuiltinSpecsDetectorMinGW) {
						GCCBuiltinSpecsDetectorMinGW specsDetector = (GCCBuiltinSpecsDetectorMinGW) newProvider;
						StringBuilder parameterProperty = new StringBuilder(specsDetector.getProperty("parameter"));

						int startOfExistingVersionOption = parameterProperty.indexOf("-std=c++");
						if (startOfExistingVersionOption > -1) {
							int endOfExistingVersionOption = parameterProperty.indexOf(" ",
									startOfExistingVersionOption);
							if (endOfExistingVersionOption > -1) {
								// + 1 at the end to also remove the space.
								// There is still a space at the beginning and we do
								// not want two spaces
								parameterProperty = parameterProperty.replace(startOfExistingVersionOption,
										endOfExistingVersionOption + 1, "");
							}
						}

						specsDetector.setProperty("parameter",
								parameterProperty + " -std=" + selectedVersion.getCompilerVersionString());
						LanguageSettingsManager.setStoringEntriesInProjectArea(specsDetector, true);

						LanguageSettingsManager.isStoringEntriesInProjectArea(specsDetector);

						ICProjectDescription pDesc = CDTPropertyManager.getProjectDescription(project);
						ICConfigurationDescription[] cfgDescs = (pDesc == null) ? null : pDesc.getConfigurations();
						for (ICConfigurationDescription configDescription : cfgDescs) {
							if (configDescription instanceof ILanguageSettingsProvidersKeeper) {
								List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) configDescription)
										.getLanguageSettingProviders();
								ArrayList<ILanguageSettingsProvider> newProviders = new ArrayList<ILanguageSettingsProvider>(
										providers);

								newProviders.remove(minGWProvider);
								newProviders.add(specsDetector);
								((ILanguageSettingsProvidersKeeper) configDescription)
										.setLanguageSettingProviders(newProviders);

							}
						}
						CDTPropertyManager.performOk(this);
					}
					// ICProjectDescription pDesc =
					// CDTPropertyManager.getProjectDescription(project);
					// ICConfigurationDescription[] cfgDescs = (pDesc == null) ?
					// null : pDesc.getConfigurations();
					// for (ICConfigurationDescription configDescription :
					// cfgDescs) {
					// List<ICLanguageSettingEntry> settings =
					// newProvider.getSettingEntries(configDescription,
					// project, "org.eclipse.cdt.core.g++");
					// if (settings != null) {
					// settings.isEmpty();
					// }
					// }
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Updating Provider");

	}
}
