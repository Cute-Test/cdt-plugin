package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CppVersions;

public class CppVersionSelectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CppVersionSelectionPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Selection of default C++ version and their actions when creating new projects.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
	 * types of preferences. Each field editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		String[][] comboVersions = new String[CppVersions.values().length][2];

		CppVersions[] possibleCppVersions = CppVersions.values();
		for (int i = 0; i < possibleCppVersions.length; i++) {
			CppVersions cppVersion = possibleCppVersions[i];
			comboVersions[i][0] = cppVersion.getVersionString();
			comboVersions[i][1] = cppVersion.toString();
		}
		ComboFieldEditor cppVersionCombo = new ComboFieldEditor(PreferenceConstants.DEFAULT_CPP_VERSION_FOR_WORKSPACE,
				"Default C++ &Version", comboVersions, getFieldEditorParent());
		addField(cppVersionCombo);

		// addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN, "&An example of a boolean preference",
		// getFieldEditorParent()));
		//
		// addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE, "An example of a multiple-choice
		// preference",
		// 1, new String[][] { { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } }, getFieldEditorParent()));
		// addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

}