package ch.hsr.ifs.cute.elevenator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.dialogs.ViewContentProvider;

public class SelectVersionWizardPage extends WizardPage {

	private final class DialectBasedSetting {
		private String name;
		private List<DialectBasedSetting> subsettings = new ArrayList<DialectBasedSetting>();

		public DialectBasedSetting(String name) {
			this.name = name;
		}

		public void addSubsetting(DialectBasedSetting subsetting) {
			subsettings.add(subsetting);
		}

		public String getName() {
			return name;
		}

		public boolean hasSubsettings() {
			return !subsettings.isEmpty();
		}

		public List<DialectBasedSetting> getSubsettings() {
			return subsettings;
		}
	}

	private final class DialectBasedSettingsProvider implements ITreeContentProvider, ILabelProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

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
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public Image getImage(Object element) {
			// TODO Auto-generated method stub
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
		System.err.println("WizardPage1");
		setMessage("Select the C++ standard version for this project");
		setTitle("C++ Version");
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
		System.err.println("Creating Control");
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

		Combo combo = new Combo(versionSelector, SWT.READ_ONLY);
		GridData comboLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		comboLayoutData.verticalIndent = INDENT;
		combo.setLayoutData(comboLayoutData);
		combo.add("C++ 03");
		combo.add("C++ 11");
		combo.add("C++ 14");
		combo.add("C++ 17");
		combo.select(0);
		combo.setFont(font);

		Group modificationsGroup = new Group(composite, SWT.NONE);
		modificationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modificationsGroup.setText("Modifications:");
		modificationsGroup.setLayout(new GridLayout());

		TreeViewer tree = new TreeViewer(modificationsGroup, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		DialectBasedSettingsProvider provider = new DialectBasedSettingsProvider();
		tree.setContentProvider(provider);
		tree.setLabelProvider(provider);

		DialectBasedSetting settings = createSettings();

		tree.setInput(settings);

		// TreeItem setCompilerFlag = new TreeItem(tree, SWT.NONE);
		// setCompilerFlag.setText("Set Compiler Flags");
		// TreeItem enableCodanMarkers = new TreeItem(tree, SWT.NONE);
		// enableCodanMarkers.setText("Enable Codan Markers");
		// TreeItem enableElevator = new TreeItem(enableCodanMarkers, SWT.NONE);
		// enableElevator.setText("Enable Elevator Markers");
		// TreeItem enablePointerminator = new TreeItem(enableCodanMarkers,
		// SWT.NONE);
		// enablePointerminator.setText("Enable Pointerminator Markers");
		// TreeItem setIndexFlag = new TreeItem(tree, SWT.NONE);
		// setIndexFlag.setText("Set Index Flag");

		setControl(composite);
	}

	private DialectBasedSetting createSettings() {
		DialectBasedSetting dialectBasedSetting = new DialectBasedSetting("C++ 11 Settings");

		DialectBasedSetting setCompilerFlag = new DialectBasedSetting("Set Compiler Flag");
		dialectBasedSetting.addSubsetting(setCompilerFlag);

		DialectBasedSetting enableCodanMarkers = new DialectBasedSetting("Enable Codan Markers");
		dialectBasedSetting.addSubsetting(enableCodanMarkers);
		DialectBasedSetting enableElevator = new DialectBasedSetting("Enable Elevator");
		enableCodanMarkers.addSubsetting(enableElevator);
		DialectBasedSetting enablePointerminator = new DialectBasedSetting("Enable Pointerminator");
		enableCodanMarkers.addSubsetting(enablePointerminator);

		DialectBasedSetting setIndexFlag = new DialectBasedSetting("Set Index Flag");
		dialectBasedSetting.addSubsetting(setIndexFlag);

		return dialectBasedSetting;
	}

	@Override
	public void setWizard(IWizard newWizard) {
		// TODO Auto-generated method stub
		super.setWizard(newWizard);
	}

	
}