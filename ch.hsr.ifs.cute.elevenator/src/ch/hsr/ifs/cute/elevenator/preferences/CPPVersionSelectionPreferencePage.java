package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CPPVersionCheckedTreeFieldEditor;
import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;
import ch.hsr.ifs.cute.elevenator.EvaluateContributions;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;

public class CPPVersionSelectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	// TODO save all other versions when apply is hit

	private ComboFieldEditor versionCombo;
	private CPPVersionCheckedTreeFieldEditor modificationTree;

	public CPPVersionSelectionPreferencePage() {
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
		versionCombo = new ComboFieldEditor(CppVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT,
				"Default C++ &Version", comboVersions, getFieldEditorParent());
		addField(versionCombo);

		DialectBasedSetting rootSettings = null; // getPreferenceStore().get

		if (rootSettings == null) {
			rootSettings = EvaluateContributions.createSettings(CPPVersion.DEFAULT);
		}

		String selectedVersion = getPreferenceStore()
				.getString(CppVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		modificationTree = new CPPVersionCheckedTreeFieldEditor(getFieldEditorParent(), rootSettings);
		addField(modificationTree);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() == versionCombo) {
			modificationTree.changeVersion(CPPVersion.valueOf(event.getNewValue().toString()));
		}
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}