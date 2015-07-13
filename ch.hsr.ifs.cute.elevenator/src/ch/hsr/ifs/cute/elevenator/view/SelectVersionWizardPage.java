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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;
import ch.hsr.ifs.cute.elevenator.EvaluateContributions;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.operation.ChangeCompilerFlagOperation;
import ch.hsr.ifs.cute.elevenator.operation.ChangeIndexFlagOperation;

public class SelectVersionWizardPage extends WizardPage {

	private static final int INDENT = 15;

	private VersionSelectionGridCombo versionCombo;
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

		versionCombo = new VersionSelectionGridCombo(composite, "C++ Version");
		versionCombo.getCombo().addSelectionListener(new SelectionAdapter() {
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

		ToolBar toolBar = new ToolBar(modificationsGroup, SWT.NONE);

		ToolItem selectAll = new ToolItem(toolBar, SWT.PUSH);
		Image selectAllIcon = new Image(parent.getDisplay(), getClass().getResourceAsStream("/icons/select_all.png"));
		selectAll.setImage(selectAllIcon);
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}
		});

		ToolItem deselectAll = new ToolItem(toolBar, SWT.PUSH);
		Image deselectAllIcon = new Image(parent.getDisplay(),
				getClass().getResourceAsStream("/icons/deselect_all.png"));
		deselectAll.setImage(deselectAllIcon);
		deselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}
		});

		updateSettings();

		setControl(composite);
	}

	private void selectAll(boolean select) {
		CPPVersion selectedVersion = getSelectedVersion();
		if (settingStore.containsKey(selectedVersion)) {
			DialectBasedSetting setting = settingStore.get(selectedVersion);
			setting.setChecked(select);
			modificationTree.refresh();
		}
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
		return (CPPVersion) versionCombo.getCombo().getData(versionCombo.getCombo().getText());
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