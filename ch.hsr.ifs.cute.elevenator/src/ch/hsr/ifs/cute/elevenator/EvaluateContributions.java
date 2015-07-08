package ch.hsr.ifs.cute.elevenator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class EvaluateContributions {
	private static final String IVERSIONMODIFICATOR_ID = "ch.hsr.ifs.cute.elevenator.versionmodification";

	public static DialectBasedSetting createSettings(CPPVersion selectedVersion) {

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(IVERSIONMODIFICATOR_ID);

		DialectBasedSetting settings = new DialectBasedSetting(selectedVersion.getVersionString() + " Settings");

		for (IConfigurationElement configElement : config) {

			boolean supportedVersion = false;

			String versionName = configElement.getName();
			if (versionName.equals("ALL_VERSIONS")) {
				supportedVersion = true;
			} else {
				CPPVersion version = CPPVersion.valueOf(versionName);
				supportedVersion = version.equals(selectedVersion);
			}

			if (supportedVersion) {
				System.out.println("Reading extensions for: " + selectedVersion);

				for (IConfigurationElement childElement : configElement.getChildren()) {
					createChildSettings(childElement, settings);
				}
			}
		}
		return settings;
	}

	private static void createChildSettings(IConfigurationElement element, DialectBasedSetting parentSettings) {

		IVersionModificationOperation versionModification = extractVersionModification(element);

		String settingName = element.getAttribute("name");
		DialectBasedSetting settings = new DialectBasedSetting(settingName, versionModification);
		parentSettings.addSubsetting(settings);

		for (IConfigurationElement childElement : element.getChildren()) {
			createChildSettings(childElement, settings);
		}

	}

	private static IVersionModificationOperation extractVersionModification(IConfigurationElement element) {
		try {
			if (!element.getName().equals("version_modification")) {
				return null;
			}
			if (element.getAttribute("class") == null) {
				return null;
			}

			final Object o = element.createExecutableExtension("class");
			if (o instanceof IVersionModificationOperation) {
				System.out.println("Found version modification: " + o.getClass().toString());
				return (IVersionModificationOperation) o;
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}
}
