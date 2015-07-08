package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CPPVersion;

public class CppVersionSelectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CppVersionSelectionPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Selection of default C++ version and their actions when creating new projects.");
	}

	@Override
	public void createFieldEditors() {
		String[][] comboVersions = new String[CPPVersion.values().length][2];

		CPPVersion[] possibleCPPVersions = CPPVersion.values();
		for (int i = 0; i < possibleCPPVersions.length; i++) {
			CPPVersion version = possibleCPPVersions[i];
			comboVersions[i][0] = version.getVersionString();
			comboVersions[i][1] = version.toString();
		}
		ComboFieldEditor versionCombo = new ComboFieldEditor(
				CppVersionPreferenceConstants.DEFAULT_CPP_VERSION_FOR_WORKSPACE, "Default C++ &Version", comboVersions,
				getFieldEditorParent());
		addField(versionCombo);

		// addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN, "&An example of a boolean preference",
		// getFieldEditorParent()));
		//
		// addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE, "An example of a multiple-choice
		// preference",
		// 1, new String[][] { { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } }, getFieldEditorParent()));
		// addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}