package ch.hsr.ifs.cute.elevenator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.PreferencesUtil;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;
import ch.hsr.ifs.cute.elevenator.EvaluateContributions;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.preferences.CPPVersionPreferenceConstants;
import ch.hsr.ifs.cute.elevenator.preferences.CPPVersionSelectionPreferencePage;
import ch.hsr.ifs.cute.elevenator.view.TreeSelectionToolbar.ISelectionToolbarAction;

public class SelectVersionWizardPage extends WizardPage implements ISelectionToolbarAction {

	private VersionSelectionCombo versionCombo;
	private CheckboxTreeViewer modificationTree;
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

		Link workspaceSettingsLink = createLink(composite, "Configure Workspace Settings...");
		workspaceSettingsLink.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));

		Group modificationsGroup = new Group(composite, SWT.NONE);
		modificationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		modificationsGroup.setText("Modifications:");
		modificationsGroup.setLayout(new GridLayout(1, false));

		DialectBasedSettingsProvider provider = new DialectBasedSettingsProvider();

		modificationTree = createTreeViewer(modificationsGroup);
		modificationTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modificationTree.setContentProvider(provider);
		modificationTree.setLabelProvider(provider);
		modificationTree.setCheckStateProvider(provider);
		modificationTree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				DialectBasedSetting setting = (DialectBasedSetting) event.getElement();
				setting.setChecked(event.getChecked());
				modificationTree.refresh();
			}
		});

		TreeSelectionToolbar selectionToolbar = new TreeSelectionToolbar(modificationsGroup, this, SWT.NONE);
		selectionToolbar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		updateSettings();

		setControl(composite);
	}

	@Override
	public void selectAll(boolean select) {
		CPPVersion selectedVersion = versionCombo.getSelectedVersion();
		DialectBasedSetting setting = settingStore.get(selectedVersion);
		if (setting != null) {
			setting.setChecked(select);
			modificationTree.refresh();
		}
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

	public Collection<DialectBasedSetting> getCheckedModifications() {
		if (modificationTree != null) {
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
		} else {
			DialectBasedSetting defaultRootSetting = EvaluateContributions.createSettings(getSelectedVersion());
			return getCheckedSettings(defaultRootSetting);
		}
	}

	private Collection<DialectBasedSetting> getCheckedSettings(DialectBasedSetting parentSetting) {
		List<DialectBasedSetting> checkedModification = new ArrayList<>();
		for (DialectBasedSetting sub : parentSetting.getSubsettings()) {
			if (sub.hasSubsettings()) {
				checkedModification.addAll(getCheckedSettings(sub));
			} else if (sub.isChecked()) {
				checkedModification.add(sub);
			}
		}
		return checkedModification;
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
		filteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return (CheckboxTreeViewer) filteredTree.getViewer();
	}

	private Link createLink(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil
						.createPreferenceDialogOn(getShell(), CPPVersionSelectionPreferencePage.PAGE_ID, null, null)
						.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				PreferencesUtil
						.createPreferenceDialogOn(getShell(), CPPVersionSelectionPreferencePage.PAGE_ID, null, null)
						.open();
			}
		});
		return link;
	}
}