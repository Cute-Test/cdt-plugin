package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CPPVersionCheckedTreeFieldEditor;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.view.VersionSelectionComboWithLabel;

public class CPPVersionSelectionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private VersionSelectionComboWithLabel versionCombo;
	private CPPVersionCheckedTreeFieldEditor modificationTree;

	public CPPVersionSelectionPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Selection of default C++ version and their actions when creating new projects.");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite top = new Composite(parent, SWT.LEFT);

		// Sets the layout data for the top composite's
		// place in its parent's layout.
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Sets the layout for the top composite's
		// children to populate.
		top.setLayout(new GridLayout());

		versionCombo = new VersionSelectionComboWithLabel(top, "C++ Version");
		versionCombo.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CPPVersion selectedVersion = versionCombo.getSelectedVersion();
				modificationTree.changeVersion(selectedVersion);
			}
		});

		String selectedVersionString = getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion selectedVersion = CPPVersion.valueOf(selectedVersionString);
		modificationTree = new CPPVersionCheckedTreeFieldEditor(top, selectedVersion);

		modificationTree.setPage(this);
		modificationTree.setPreferenceStore(getPreferenceStore());
		modificationTree.load();

		return top;
	}

	@Override
	protected void performDefaults() {
		modificationTree.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		modificationTree.store();
		return true;
	}
}