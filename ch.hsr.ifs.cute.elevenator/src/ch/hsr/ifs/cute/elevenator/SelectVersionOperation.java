package ch.hsr.ifs.cute.elevenator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.language.settings.providers.GCCBuiltinSpecsDetectorMinGW;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

public class SelectVersionOperation implements IRunnableWithProgress {

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		IWizardPage[] pages = MBSCustomPageManager.getCustomPages();
		IWizard wizard = pages[0].getWizard();
		if (wizard instanceof CDTCommonProjectWizard) {
			CDTCommonProjectWizard projectWizard = (CDTCommonProjectWizard) wizard;
			IProject project = projectWizard.getProject(false);
			updateStandard(project);
			updateProvider(project);
		}
	}

	private void updateProvider(IProject project) {
		ILanguageSettingsProvider minGWProvider = LanguageSettingsManager
				.getWorkspaceProvider("org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetectorMinGW");
		// ICConfigurationDescription[] cfgDescs = (pDesc == null) ? null :
		// pDesc.getConfigurations();
		// for (ICConfigurationDescription configDescription : cfgDescs) {
		// if (configDescription instanceof ILanguageSettingsProvidersKeeper) {
		// String cfgId = configDescription.getId();
		// List<ILanguageSettingsProvider> initialProviders =
		// ((ILanguageSettingsProvidersKeeper) configDescription)
		// .getLanguageSettingProviders();
		// for (ILanguageSettingsProvider provider : initialProviders) {
		if (minGWProvider != null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(minGWProvider);
			if (rawProvider instanceof ILanguageSettingsEditableProvider
					&& !LanguageSettingsManager
							.isStoringEntriesInProjectArea((LanguageSettingsSerializableProvider) rawProvider)) {

				try {
					ILanguageSettingsEditableProvider newProvider = ((ILanguageSettingsEditableProvider) rawProvider)
							.cloneShallow();
					if (newProvider instanceof GCCBuiltinSpecsDetectorMinGW){
						GCCBuiltinSpecsDetectorMinGW specsDetector = (GCCBuiltinSpecsDetectorMinGW) newProvider;
						String parameterProperty = specsDetector.getProperty("parameter");
						specsDetector.setProperty("parameter", parameterProperty + " -std=c++0x");
						LanguageSettingsManager.setStoringEntriesInProjectArea(specsDetector, true);
						
						LanguageSettingsManager.isStoringEntriesInProjectArea((LanguageSettingsSerializableProvider) specsDetector);
						
						ICProjectDescription pDesc = CDTPropertyManager.getProjectDescription(project);
						ICConfigurationDescription[] cfgDescs = (pDesc == null) ? null : pDesc.getConfigurations();
						for (ICConfigurationDescription configDescription : cfgDescs) {
							if (configDescription instanceof ILanguageSettingsProvidersKeeper) {
								List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) configDescription).getLanguageSettingProviders();
								ArrayList<ILanguageSettingsProvider> newProviders = new ArrayList<ILanguageSettingsProvider>(providers);
								
								newProviders.remove(minGWProvider);
								newProviders.add(specsDetector);
								((ILanguageSettingsProvidersKeeper) configDescription).setLanguageSettingProviders(newProviders);
								
							}
						}
						CDTPropertyManager.performOk(this);
//						ICConfigurationDescription settingConfiguration = CDTPropertyManager.getProjectDescription(project).getDefaultSettingConfiguration();
						
//						if (settingConfiguration instanceof ILanguageSettingsProvidersKeeper) {
//							ILanguageSettingsProvidersKeeper providersKeeper = (ILanguageSettingsProvidersKeeper) settingConfiguration;
//							providersKeeper.setLanguageSettingProviders(providers)
//						}
//						if (sd instanceof ILanguageSettingsProvidersKeeper && dd instanceof ILanguageSettingsProvidersKeeper) {
//							List<ILanguageSettingsProvider> newProviders = ((ILanguageSettingsProvidersKeeper) sd).getLanguageSettingProviders();
//							((ILanguageSettingsProvidersKeeper) dd).setLanguageSettingProviders(newProviders);
//						}
					}
//					ICProjectDescription pDesc = CDTPropertyManager.getProjectDescription(project);
//					ICConfigurationDescription[] cfgDescs = (pDesc == null) ? null : pDesc.getConfigurations();
//					for (ICConfigurationDescription configDescription : cfgDescs) {
//						List<ICLanguageSettingEntry> settings = newProvider.getSettingEntries(configDescription,
//								project, "org.eclipse.cdt.core.g++");
//						if (settings != null) {
//							settings.isEmpty();
//						}
//					}
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("copied");
			}

		}
		System.out.println("Updating Provider");
	}

	private void updateStandard(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			ITool[] tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.compiler");
			for (ITool tool : tools) {
				IOption option = tool.getOptionById("gnu.cpp.compiler.option.dialect.std");
				String value = "ISO C++11 (-std=c++0x)";
				ManagedBuildManager.setOption(config, tool, option, value);
			}
		}
	}
}