package ch.hsr.ifs.cute.elevenator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;
import ch.hsr.ifs.cute.elevenator.EvaluateContributions;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.operation.ChangeCompilerFlagOperation;
import ch.hsr.ifs.cute.elevenator.operation.ChangeIndexFlagOperation;
import ch.hsr.ifs.cute.elevenator.preferences.CppVersionPreferenceConstants;

public class SelectVersionWizardPage extends WizardPage {

	private static final int INDENT = 15;

	private Combo versionCombo;
	private CheckboxTreeViewer modificationTree;
	private Map<CPPVersion, DialectBasedSetting> settingStore = new HashMap<>();

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

		DialectBasedSettingsProvider treeContentProvider = new DialectBasedSettingsProvider();

		modificationTree = createTreeViewer(modificationsGroup);
		modificationTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modificationTree.setContentProvider(treeContentProvider);
		modificationTree.setLabelProvider(treeContentProvider);
		modificationTree.setCheckStateProvider(treeContentProvider);
		modificationTree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				DialectBasedSetting setting = (DialectBasedSetting) event.getElement();
				setting.setChecked(event.getChecked());
				modificationTree.refresh();
			}
		});

		updateSettings();

		setControl(composite);
	}

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
	}

	protected CheckboxTreeViewer createTreeViewer(Composite parent) {
		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);
		FilteredTree filteredTree = new FilteredTree(parent,
				SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION, filter, true) {
			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new CheckboxTreeViewer(parent, style);
			}
		};
		return (CheckboxTreeViewer) filteredTree.getViewer();
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

	public CPPVersion getSelectedVersion() {
		return (CPPVersion) versionCombo.getData(versionCombo.getText());
	}

	public List<DialectBasedSetting> getCheckedModifications() {
		Object[] checkedElements = modificationTree.getCheckedElements();
		List<DialectBasedSetting> checkedModifications = new ArrayList<>();

		for (Object elem : checkedElements) {
			if (elem instanceof DialectBasedSetting) {
				DialectBasedSetting setting = (DialectBasedSetting) elem;
				if (!setting.hasSubsettings()) {
					checkedModifications.add(setting);
				}
			}
		}
		return checkedModifications;
	}

	@Override
	public void setWizard(IWizard newWizard) {
		super.setWizard(newWizard);
	}
}