package ch.hsr.ifs.cute.elevenator.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.preferences.CPPVersionPreferenceConstants;

public class VersionSelectionComboWithLabel extends Composite {
	private Combo versionCombo;

	public VersionSelectionComboWithLabel(Composite parent, String labelText, Control thirdControl) {
		super(parent, SWT.NONE);
		Composite versionSelector = new Composite(parent, SWT.NONE);
		versionSelector.setFont(parent.getFont());
		versionSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		versionSelector.setLayout(new GridLayout(3, false));

		Label label = new Label(versionSelector, SWT.NONE);
		label.setText(labelText);
		label.setFont(versionSelector.getFont());

		versionCombo = new Combo(versionSelector, SWT.READ_ONLY);

		for (CPPVersion cppVersion : CPPVersion.values()) {
			versionCombo.add(cppVersion.getVersionString());
			versionCombo.setData(cppVersion.getVersionString(), cppVersion);
		}

		String defaultCppVersionString = Activator.getDefault().getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion versionToSelect = CPPVersion.valueOf(defaultCppVersionString);
		versionCombo.select(versionToSelect.ordinal());
		versionCombo.setFont(versionSelector.getFont());

		if (thirdControl != null) {
			thirdControl.setParent(versionSelector);
		}
	}

	public CPPVersion getSelectedVersion() {
		return (CPPVersion) versionCombo.getData(versionCombo.getText());
	}

	public Combo getCombo() {
		return versionCombo;
	}
}
