package ch.hsr.ifs.cute.elevenator.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.elevenator.Activator;
import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;
import ch.hsr.ifs.cute.elevenator.operation.ChangeCompilerFlagOperation;
import ch.hsr.ifs.cute.elevenator.operation.ChangeIndexFlagOperation;
import ch.hsr.ifs.cute.elevenator.operation.DoNothingOperation;
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

	public final class DialectBasedSetting {
		private String name;
		private List<DialectBasedSetting> subsettings = new ArrayList<DialectBasedSetting>();
		private IVersionModificationOperation operation;

		public DialectBasedSetting(String name) {
			this(name, new DoNothingOperation());
		}

		public DialectBasedSetting(String name, IVersionModificationOperation operation) {
			this.name = name;
			this.operation = operation;
		}

		public void addSubsetting(DialectBasedSetting subsetting) {
			subsettings.add(subsetting);
		}

		public String getName() {
			return name;
		}

		public IVersionModificationOperation getOperation() {
			return operation;
		}

		public boolean hasSubsettings() {
			return !subsettings.isEmpty();
		}

		public List<DialectBasedSetting> getSubsettings() {
			return subsettings;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final class DialectBasedSettingsProvider implements ITreeContentProvider, ILabelProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof DialectBasedSetting) {
				return ((DialectBasedSetting) element).hasSubsettings();
			}
			return false;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof DialectBasedSetting) {
				List<DialectBasedSetting> subsettings = ((DialectBasedSetting) parentElement).getSubsettings();
				return subsettings.toArray(new Object[subsettings.size()]);
			}
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof DialectBasedSetting) {
				return ((DialectBasedSetting) element).getName();
			}
			return "<unknown>";
		}
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
				.getString(CppVersionPreferenceConstants.DEFAULT_CPP_VERSION_FOR_WORKSPACE);
		CPPVersion versionToSelect = CPPVersion.valueOf(defaultCppVersionString);
		versionCombo.select(versionToSelect.ordinal());

		versionCombo.setFont(font);

		Group modificationsGroup = new Group(composite, SWT.NONE);
		modificationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modificationsGroup.setText("Modifications:");
		modificationsGroup.setLayout(new GridLayout());

		modificationTree = new CheckboxTreeViewer(modificationsGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		modificationTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		DialectBasedSettingsProvider provider = new DialectBasedSettingsProvider();
		modificationTree.setContentProvider(provider);
		modificationTree.setLabelProvider(provider);

		DialectBasedSetting settings = createSettings();

		modificationTree.setInput(settings);
		setControl(composite);
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