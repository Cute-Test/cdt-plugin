package ch.hsr.ifs.cute.elevenator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.preferences.CppVersionPreferenceConstants;
import ch.hsr.ifs.cute.elevenator.view.DialectBasedSettingsProvider;

/**
 * Field editor that can be used for multiple settings. Call {@link #changeVersion(CPPVersion)} to show settings for
 * another C++ version.
 */
public class CPPVersionCheckedTreeFieldEditor extends CheckedTreeEditor {
	private DialectBasedSetting currentSettings;
	private CPPVersion selectedVersion;
	private Map<CPPVersion, DialectBasedSetting> settingStore = new HashMap<>();

	public CPPVersionCheckedTreeFieldEditor(Composite parent, DialectBasedSetting settings) {
		super("this_elevenator_preference_will_not_be_used", "Settings", parent);
		setEmptySelectionAllowed(true);

		DialectBasedSettingsProvider provider = new DialectBasedSettingsProvider();
		getTreeViewer().setContentProvider(provider);
		getTreeViewer().setLabelProvider(provider);
		getTreeViewer().setCheckStateProvider(provider);
		getTreeViewer().setInput(settings);
		currentSettings = settings;
	}

	@Override
	protected void doLoad() {
		// load from our own store since the user could have changed something, switched the version and this is not yet
		// stored. selectedVersion must already be set correctly
		DialectBasedSetting settingsForVersion = getSettings();
		getViewer().setInput(settingsForVersion);
		currentSettings = settingsForVersion;
	}

	private DialectBasedSetting getSettings() {
		DialectBasedSetting rootSettingForVersion = settingStore.get(getSelectedVersion());
		if (rootSettingForVersion == null) {
			rootSettingForVersion = EvaluateContributions.createSettings(getSelectedVersion());
			settingStore.put(getSelectedVersion(), rootSettingForVersion);
		}

		return rootSettingForVersion;
	}

	@Override
	protected Object modelFromString(String s) {
		return currentSettings;
	}

	@Override
	protected String modelToString(Object model) {
		System.out.println("modelToString^");
		return null;
	}

	@Override
	protected void doStore() {
		for (DialectBasedSetting dialectRootSetting : settingStore.values()) {
			storeSetting(dialectRootSetting);
		}
	}

	private void storeSetting(DialectBasedSetting parentSetting) {
		for (DialectBasedSetting sub : parentSetting.getSubsettings()) {
			if (sub.getPreferenceName() != null) {
				getPreferenceStore().setValue(sub.getPreferenceName(), sub.isChecked());
				storeSetting(sub);
			}
		}
		if (parentSetting.getPreferenceName() != null) {
			getPreferenceStore().setValue(parentSetting.getPreferenceName(), parentSetting.isChecked());
		}
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof DialectBasedSetting) {
			DialectBasedSetting setting = (DialectBasedSetting) element;
			setting.setChecked(event.getChecked());
		}
		getTreeViewer().refresh();
	}

	public void changeVersion(CPPVersion newSelectedVersion) {
		selectedVersion = newSelectedVersion;
		load();
	}

	@Override
	protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int defaultStyle) {
		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);

		FilteredTree filteredTree = new FilteredTree(parent,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION, filter, true) {
			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new CheckboxTreeViewer(parent, style);
			}
		};
		return (CheckboxTreeViewer) filteredTree.getViewer();
	}

	public CPPVersion getSelectedVersion() {
		if (selectedVersion == null) {
			selectedVersion = CPPVersion
					.valueOf(getPreferenceStore().getString(CppVersionPreferenceConstants.ELEVENATOR_VERSION_DEFAULT));
		}

		return selectedVersion;
	}

}
