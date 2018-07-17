/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;


/**
 * @author Emanuel Graf
 *
 */
class PreferenceInitializer : AbstractPreferenceInitializer() {

   override fun initializeDefaultPreferences() =  TestFrameworkPlugin.default?.getPreferenceStore()?.setDefault(SHOW_WHITESPACES, false) ?: Unit

}
