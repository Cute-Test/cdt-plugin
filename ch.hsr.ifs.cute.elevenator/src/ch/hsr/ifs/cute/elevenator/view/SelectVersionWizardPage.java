package ch.hsr.ifs.cute.elevenator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;
import ch.hsr.ifs.cute.elevenator.EvaluateContributions;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.preferences.CPPVersionPreferenceConstants;

public class SelectVersionWizardPage extends WizardPage {

	private VersionSelectionCombo versionCombo;
	private ModificationTree modificationTree;
	private Map<CPPVersion, DialectBasedSetting> settingStore = new HashMap<>();

	public SelectVersionWizardPage() {
		super("C++ version selection for project");
		setMessage("Select the C++ standard version for this project");
		setTitle("C++ Version");
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(2, false));

		versionCombo = new VersionSelectionCombo(composite, "C++ Version", SWT.NONE);
		versionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSettings();
			}
		});

		WorkspaceSettingsLink workspaceSettingsLink = new WorkspaceSettingsLink(composite, SWT.NONE);
		workspaceSettingsLink.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));

		modificationTree = new ModificationTree(composite, SWT.NONE);
		modificationTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		updateSettings();

		setControl(composite);
	}

	public void refreshSettings() {
		for (CPPVersion version : settingStore.keySet()) {
			DialectBasedSetting setting = EvaluateContributions.createSettings(version);
			settingStore.put(version, setting);
		}
		updateSettings();
	}

	private void updateSettings() {
		CPPVersion selectedVersion = versionCombo.getSelectedVersion();

		DialectBasedSetting setting = settingStore.get(selectedVersion);
		if (setting == null) {
			setting = EvaluateContributions.createSettings(selectedVersion);
			settingStore.put(selectedVersion, setting);
		}
		modificationTree.setInput(setting);
	}

	public Collection<DialectBasedSetting> getVersionModifications() {
		CPPVersion selectedVersion = getSelectedVersion();

		DialectBasedSetting rootSetting = settingStore.get(selectedVersion);
		if (rootSetting == null) {
			rootSetting = EvaluateContributions.createSettings(selectedVersion);
		}
		return listSettings(rootSetting);
	}

	private Collection<DialectBasedSetting> listSettings(DialectBasedSetting setting) {
		List<DialectBasedSetting> settingList = new ArrayList<>();
		for (DialectBasedSetting subSetting : setting.getSubsettings()) {
			if (subSetting.hasSubsettings()) {
				settingList.addAll(listSettings(subSetting));
			} else {
				settingList.add(subSetting);
			}
		}
		return settingList;
	}

	public CPPVersion getSelectedVersion() {
		if (versionCombo != null && !versionCombo.isDisposed()) {
			return versionCombo.getSelectedVersion();
		} else {
			String defaultCppVersionString = Activator.getDefault().getPreferenceStore()
					.getString(CPPVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
			return CPPVersion.valueOf(defaultCppVersionString);
		}
	}
}