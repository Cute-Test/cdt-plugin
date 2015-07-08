package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;

/**
 * Class used to initialize default preference values.
 */
public class CppVersionPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(CppVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT,
				CPPVersion.DEFAULT.toString());
	}

}
