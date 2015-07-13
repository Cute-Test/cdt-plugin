package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CPPVersionCheckedTreeFieldEditor;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;

public class CPPVersionSelectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
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
		versionCombo = new ComboFieldEditor(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT,
				"Default C++ &Version", comboVersions, getFieldEditorParent());
		addField(versionCombo);

		Button button = new Button(getFieldEditorParent(), 0);

		String selectedVersionString = getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion selectedVersion = CPPVersion.valueOf(selectedVersionString);
		modificationTree = new CPPVersionCheckedTreeFieldEditor(getFieldEditorParent(), selectedVersion);
		addField(modificationTree);

		button.setText("Set " + selectedVersion.getVersionString() + " as default.");
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("button selected");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

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