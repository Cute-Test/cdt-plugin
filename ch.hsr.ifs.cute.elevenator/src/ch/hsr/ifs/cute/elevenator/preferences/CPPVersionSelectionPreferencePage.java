package ch.hsr.ifs.cute.elevenator.preferences;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.CPPVersionCheckedTreeFieldEditor;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.view.SelectVersionWizardPage;
import ch.hsr.ifs.cute.elevenator.view.TreeSelectionToolbar;
import ch.hsr.ifs.cute.elevenator.view.TreeSelectionToolbar.ISelectionToolbarAction;
import ch.hsr.ifs.cute.elevenator.view.VersionSelectionCombo;

public class CPPVersionSelectionPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage, ISelectionToolbarAction {

	private VersionSelectionCombo versionCombo;
	private CPPVersionCheckedTreeFieldEditor modificationTree;
	private Button defaultVersionButton;

	public static final String PAGE_ID = "ch.hsr.ifs.cute.elevenator.preferences.CPPVersionSelectionPreferencePage";
	private Label selectedDefaultVersionLabel;

	public CPPVersionSelectionPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Selection of default C++ version and their actions when creating new projects.");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		// composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(new GridLayout(2, false));

		versionCombo = new VersionSelectionCombo(composite, "C++ Version", SWT.NONE);
		versionCombo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		versionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CPPVersion selectedVersion = versionCombo.getSelectedVersion();
				modificationTree.changeVersion(selectedVersion);
				updateDefaultVersionButton();
			}
		});

		defaultVersionButton = new Button(composite, SWT.PUSH);
		defaultVersionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		defaultVersionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPreferenceStore().setValue(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT,
						versionCombo.getSelectedVersion().toString());
				updateDefaultVersionButton();
				updateDefaultVersionLabel();
			}

		});
		updateDefaultVersionButton();

		selectedDefaultVersionLabel = new Label(composite, SWT.NONE);
		selectedDefaultVersionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 2, 1));
		updateDefaultVersionLabel();

		Group modificationsGroup = new Group(composite, SWT.NONE);
		modificationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		modificationsGroup.setText("Settings:");
		modificationsGroup.setLayout(new GridLayout(1, false));

		String selectedVersionString = getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion selectedVersion = CPPVersion.valueOf(selectedVersionString);
		modificationTree = new CPPVersionCheckedTreeFieldEditor(modificationsGroup, selectedVersion);
		modificationTree.setPage(this);
		modificationTree.setPreferenceStore(getPreferenceStore());
		modificationTree.load();

		TreeSelectionToolbar selectionToolbar = new TreeSelectionToolbar(modificationsGroup, this, SWT.NONE);
		selectionToolbar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		return composite;
	}

	private void updateDefaultVersionLabel() {
		String versionString = selectedVersionString();
		selectedDefaultVersionLabel.setText("Current Default Version is " + versionString);
	}

	private void updateDefaultVersionButton() {
		String versionString = selectedVersionString();
		defaultVersionButton.setText("Set " + versionString + " as Default Version");

		String storedDefaultVersionString = getPreferenceStore()
				.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		String selectedDefaultVersionString = versionCombo.getSelectedVersion().toString();
		boolean versionMatches = storedDefaultVersionString.equals(selectedDefaultVersionString);
		defaultVersionButton.setEnabled(!versionMatches);
	}

	private void updateWizardFromPreferences() {
		try {
			IWizardPage[] pages = MBSCustomPageManager.getCustomPages();
			for (IWizardPage page : pages) {
				if (page instanceof SelectVersionWizardPage) {
					((SelectVersionWizardPage) page).refreshSettings();
					break;
				}
			}
		} catch (NullPointerException e) {
			// The wizard is not open, do nothing
		}
	}

	private String selectedVersionString() {
		return versionCombo.getSelectedVersion().getVersionString();
	}

	@Override
	protected void performDefaults() {
		modificationTree.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		modificationTree.store();
		updateWizardFromPreferences();
		return true;
	}

	@Override
	public void selectAll(boolean selected) {
		modificationTree.selectAll(selected);
	}
}