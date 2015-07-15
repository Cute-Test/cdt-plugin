package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CPPVersionCheckedTreeFieldEditor;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.view.TreeSelectionToolbar;
import ch.hsr.ifs.cute.elevenator.view.TreeSelectionToolbar.ISelectionToolbarAction;
import ch.hsr.ifs.cute.elevenator.view.VersionSelectionComboWithLabel;

public class CPPVersionSelectionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private VersionSelectionComboWithLabel versionCombo;
	private CPPVersionCheckedTreeFieldEditor modificationTree;
	private Button defaultVersionButton;

	public static final String PAGE_ID = "ch.hsr.ifs.cute.elevenator.preferences.CPPVersionSelectionPreferencePage";

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

		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		top.setLayout(new GridLayout());

		defaultVersionButton = new Button(top, SWT.PUSH);
		versionCombo = new VersionSelectionComboWithLabel(top, "C++ Version", defaultVersionButton);
		GridData buttonLayoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false);
		defaultVersionButton.setLayoutData(buttonLayoutData);
		defaultVersionButton
				.setText("Set " + versionCombo.getSelectedVersion().getVersionString() + " as default version.");

		versionCombo.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				defaultVersionButton.setText(
						"Set " + versionCombo.getSelectedVersion().getVersionString() + " as default version.");
				CPPVersion selectedVersion = versionCombo.getSelectedVersion();
				modificationTree.changeVersion(selectedVersion);
			}
		});
		defaultVersionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPreferenceStore().setValue(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT,
						versionCombo.getSelectedVersion().toString());
				super.widgetSelected(e);
			}
		});

		String selectedVersionString = getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion selectedVersion = CPPVersion.valueOf(selectedVersionString);
		modificationTree = new CPPVersionCheckedTreeFieldEditor(top, selectedVersion);

		modificationTree.setPage(this);
		modificationTree.setPreferenceStore(getPreferenceStore());
		modificationTree.load();

		new TreeSelectionToolbar(top, new ISelectionToolbarAction() {
			@Override
			public void selectAll(boolean selected) {
				modificationTree.selectAll(selected);
			}
		}, SWT.NONE);

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