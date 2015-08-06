package ch.hsr.ifs.cute.charwars.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.hsr.ifs.cute.charwars.CharWarsPlugin;

public class CharWarsPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CharWarsPlugin.getDefault().getPreferenceStore();
		//store.setDefault(PreferenceConstants.SHOW_WHITESPACES, false);
	}
}
