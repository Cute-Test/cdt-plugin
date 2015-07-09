package ch.hsr.ifs.cute.elevenator.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;
import ch.hsr.ifs.cute.elevenator.EvaluateContributions;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.operation.ChangeCompilerFlagOperation;
import ch.hsr.ifs.cute.elevenator.operation.ChangeIndexFlagOperation;
import ch.hsr.ifs.cute.elevenator.preferences.CppVersionPreferenceConstants;

public class SelectVersionWizardPage extends WizardPage {

	private Combo versionCombo;
	private CheckboxTreeViewer modificationTree;

	public CPPVersion getSelectedVersion() {
		return (CPPVersion) versionCombo.getData(versionCombo.getText());
	}

	public DialectBasedSetting[] getCheckedModifications() {
		Object[] checkedElements = modificationTree.getCheckedElements();
		DialectBasedSetting[] checkedModifications = new DialectBasedSetting[checkedElements.length];

		int modificationCount = 0;
		for (Object elem : checkedElements) {
			if (elem instanceof DialectBasedSetting) {
				checkedModifications[modificationCount++] = (DialectBasedSetting) elem;
			}
		}
		return checkedModifications;
	}

	private static final int INDENT = 15;

	public SelectVersionWizardPage() {
		super("mypage");
		setMessage("Select the C++ standard version for this project");
		setTitle("C++ Version");
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite versionSelector = new Composite(composite, SWT.NONE);
		versionSelector.setLayout(new GridLayout(2, false));
		Label label = new Label(versionSelector, NONE);
		GridData labelLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		labelLayoutData.horizontalIndent = INDENT;
		labelLayoutData.verticalIndent = INDENT;
		label.setLayoutData(labelLayoutData);
		label.setText("C++ Version:");
		label.setFont(font);

		GridData comboLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		comboLayoutData.verticalIndent = INDENT;
		versionCombo = new Combo(versionSelector, SWT.READ_ONLY);
		versionCombo.setLayoutData(comboLayoutData);

		for (CPPVersion cppVersion : CPPVersion.values()) {
			versionCombo.add(cppVersion.getVersionString());
			versionCombo.setData(cppVersion.getVersionString(), cppVersion);
		}

		String defaultCppVersionString = Activator.getDefault().getPreferenceStore()
				.getString(CppVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT);
		CPPVersion versionToSelect = CPPVersion.valueOf(defaultCppVersionString);
		versionCombo.select(versionToSelect.ordinal());
		versionCombo.setFont(font);
		versionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSettings();
			}
		});

		Group modificationsGroup = new Group(composite, SWT.NONE);
		modificationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modificationsGroup.setText("Modifications:");
		modificationsGroup.setLayout(new GridLayout());

		modificationTree = new CheckboxTreeViewer(modificationsGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		modificationTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		DialectBasedSettingsProvider treeContentProvider = new DialectBasedSettingsProvider();
		modificationTree.setContentProvider(treeContentProvider);
		modificationTree.setLabelProvider(treeContentProvider);
		modificationTree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				DialectBasedSetting setting = (DialectBasedSetting) event.getElement();
				setting.setChecked(event.getChecked());
			}
		});

		updateSettings();

		setControl(composite);
	}

	private Map<CPPVersion, DialectBasedSetting> settingStore = new HashMap<>();

	private void updateSettings() {
		CPPVersion selectedVersion = getSelectedVersion();

		DialectBasedSetting settings = null;
		if (settingStore.containsKey(selectedVersion)) {
			settings = settingStore.get(selectedVersion);
		} else {
			settings = EvaluateContributions.createSettings(selectedVersion);
			settingStore.put(selectedVersion, settings);
		}

		modificationTree.setInput(settings);
		// modificationTree.expandAll();
		updateCheckedSettings(settings);
	}

	private void updateCheckedSettings(DialectBasedSetting setting) {
		modificationTree.setChecked(setting, setting.isChecked());
		if (setting.isChecked()) {
			modificationTree.expandToLevel(setting, 1);
		}
		for (DialectBasedSetting childSetting : setting.getSubsettings()) {
			updateCheckedSettings(childSetting);
		}
	}

	private DialectBasedSetting createSettings() {
		DialectBasedSetting dialectBasedSetting = new DialectBasedSetting("C++ 11 Settings");

		DialectBasedSetting setCompilerFlag = new DialectBasedSetting("Set Compiler Flag",
				new ChangeCompilerFlagOperation());
		dialectBasedSetting.addSubsetting(setCompilerFlag);

		DialectBasedSetting enableCodanMarkers = new DialectBasedSetting("Enable Codan Markers");
		dialectBasedSetting.addSubsetting(enableCodanMarkers);
		DialectBasedSetting enableElevator = new DialectBasedSetting("Enable Elevator");
		enableCodanMarkers.addSubsetting(enableElevator);
		DialectBasedSetting enablePointerminator = new DialectBasedSetting("Enable Pointerminator");
		enableCodanMarkers.addSubsetting(enablePointerminator);

		DialectBasedSetting setIndexFlag = new DialectBasedSetting("Set Index Flag", new ChangeIndexFlagOperation());
		dialectBasedSetting.addSubsetting(setIndexFlag);

		return dialectBasedSetting;
	}

	@Override
	public void setWizard(IWizard newWizard) {
		super.setWizard(newWizard);
	}
}