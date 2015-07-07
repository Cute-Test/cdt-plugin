package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CppVersions;

/**
 * Class used to initialize default preference values.
 */
public class CppVersionPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(CppVersionPreferenceConstants.DEFAULT_CPP_VERSION_FOR_WORKSPACE, CppVersions.DEFAULT.getVersionString());
	}

}
