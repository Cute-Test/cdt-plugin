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
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.view.DefaultVersionSelector;
import ch.hsr.ifs.cute.elevenator.view.ModificationTree;
import ch.hsr.ifs.cute.elevenator.view.VersionSelectionCombo;

public class CPPVersionSelectionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID = "ch.hsr.ifs.cute.elevenator.preferences.CPPVersionSelectionPreferencePage";

	private VersionSelectionCombo versionCombo;
	private ModificationTree modificationTree;
	private DefaultVersionSelector defaultVersionSelector;

	public CPPVersionSelectionPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Selection of default C++ version and their actions when creating new projects.");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		versionCombo = new VersionSelectionCombo(composite, "C++ Version", SWT.NONE);
		versionCombo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		versionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CPPVersion selectedVersion = versionCombo.getSelectedVersion();
				// modificationTree.changeVersion(selectedVersion);
				defaultVersionSelector.updateDefaultVersionButton();
			}
		});

		defaultVersionSelector = new DefaultVersionSelector(composite, versionCombo, SWT.NONE);
		defaultVersionSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		modificationTree = new ModificationTree(composite, SWT.NONE);
		modificationTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		return super.performOk();
	}

}
