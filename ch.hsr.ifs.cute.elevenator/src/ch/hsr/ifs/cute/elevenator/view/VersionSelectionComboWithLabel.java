package ch.hsr.ifs.cute.elevenator.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.preferences.CPPVersionPreferenceConstants;

public class VersionSelectionComboWithLabel extends Composite {
	private Combo versionCombo;

	private final int INDENT;

	private Composite versionSelector;

	public VersionSelectionComboWithLabel(Composite parent, String labelText, int verticalIndent) {
		super(parent, SWT.NONE);
		INDENT = verticalIndent;
		versionSelector = new Composite(parent, SWT.NONE);
		versionSelector.setLayout(new GridLayout(3, false));

		Label label = new Label(versionSelector, SWT.NONE);
		GridData labelLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		labelLayoutData.horizontalIndent = INDENT;
		labelLayoutData.verticalIndent = INDENT;
		label.setLayoutData(labelLayoutData);
		label.setText(labelText);
		label.setFont(parent.getFont());

		GridData comboLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		comboLayoutData.verticalIndent = INDENT;
		versionCombo = new Combo(versionSelector, SWT.READ_ONLY);
		versionCombo.setLayoutData(comboLayoutData);

		for (CPPVersion cppVersion : CPPVersion.values()) {
			versionCombo.add(cppVersion.getVersionString());
			versionCombo.setData(cppVersion.getVersionString(), cppVersion);
		}

		String defaultCppVersionString = Activator.getDefault().getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion versionToSelect = CPPVersion.valueOf(defaultCppVersionString);
		versionCombo.select(versionToSelect.ordinal());
	}

	public CPPVersion getSelectedVersion() {
		return (CPPVersion) versionCombo.getData(versionCombo.getText());
	}

	public Combo getCombo() {
		return versionCombo;
	}

	public Composite getComposite() {
		return versionSelector;
	}
}
