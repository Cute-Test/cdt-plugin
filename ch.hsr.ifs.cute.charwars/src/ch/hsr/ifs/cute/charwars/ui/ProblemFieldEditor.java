package ch.hsr.ifs.cute.charwars.ui;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.LaunchModeProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.core.CodanPreferencesLoader;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class ProblemFieldEditor extends FieldEditor {
	private final Composite parent;
	private CodanPreferencesLoader codanPreferencesLoader = new CodanPreferencesLoader();
	private final String problemId;
	private String fValue;
	private Group group;
	private Button enabledCheckBox;
	private Label severityLabel;
	private Combo severityCombo;
	private Label launchingLabel;
	private Button runAsYouType;
	private Button runOnFileOpen;
	private Button runOnFileSave;
	private Button runOnIncrementalBuild;
	private Button runOnFullBuild;
	private Button runOnDemand;
	
	private final static String[][] fEntryNamesAndValues = new String[][] {
		{ CodanSeverity.Error.toTranslatableString(), CodanSeverity.Error.toString() },
		{ CodanSeverity.Warning.toTranslatableString(), CodanSeverity.Warning.toString() },
		{ CodanSeverity.Info.toTranslatableString(), CodanSeverity.Info.toString() }
	};
	
    public ProblemFieldEditor(Composite parent, IProblemProfile profile, String problemId) {
    	super("<placeholder>", "", parent);
    	this.parent = parent;
    	this.problemId = problemId;
		codanPreferencesLoader.setInput(profile);
    }
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		if(group != null) {
			((GridData)group.getLayoutData()).horizontalSpan = numColumns;
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		if(group == null) {
			group = new Group(parent, SWT.SHADOW_IN);
			GridData gd1 = new GridData();
			gd1.horizontalAlignment = SWT.FILL;
			gd1.horizontalSpan = numColumns;
			gd1.grabExcessHorizontalSpace = true;
			group.setLayoutData(gd1);
			
			GridLayout gridLayout = new GridLayout(4, false);
			group.setLayout(gridLayout);
			
			enabledCheckBox = new Button(group, SWT.CHECK);
			enabledCheckBox.setText("Enabled");
			GridData gd2 = new GridData();
			gd2.horizontalSpan = 4;
			enabledCheckBox.setLayoutData(gd2);
			enabledCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateEnabledState();
				}
			});
			
			severityLabel = new Label(group, SWT.LEFT);
			severityLabel.setText("Severity:");
			GridData gd3 = new GridData();
			gd3.horizontalSpan = 3;
			getSeverityCombo(group).setLayoutData(gd3);;
			
			launchingLabel = new Label(group, SWT.LEFT);
			launchingLabel.setText("Triggers:");
			
			runAsYouType = createLaunchModeCheckBox("Run as you type", CheckerLaunchMode.RUN_AS_YOU_TYPE);
			runOnFileOpen = createLaunchModeCheckBox("Run on file open", CheckerLaunchMode.RUN_ON_FILE_OPEN);
			runOnFileSave = createLaunchModeCheckBox("Run on file save", CheckerLaunchMode.RUN_ON_FILE_SAVE);
			
			//empty, dummy label to add empty cell in grid layout
			new Label(group, SWT.LEFT);
			
			runOnIncrementalBuild = createLaunchModeCheckBox("Run on incremental build", CheckerLaunchMode.RUN_ON_INC_BUILD);
			runOnFullBuild = createLaunchModeCheckBox("Run on full build", CheckerLaunchMode.RUN_ON_FULL_BUILD);
			runOnDemand = createLaunchModeCheckBox("Run on demand", CheckerLaunchMode.RUN_ON_DEMAND);
		}
	}
	
	private Button createLaunchModeCheckBox(String title, final CheckerLaunchMode launchMode) {
		final Button checkBox = new Button(group, SWT.CHECK);
		checkBox.setText(title);
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IProblem problem = getProblem();
				IProblemPreference info = problem.getPreference();
				if(info instanceof RootProblemPreference) {
					LaunchModeProblemPreference launchModes = ((RootProblemPreference) info).getLaunchModePreference();
					launchModes.addLaunchMode(launchMode, checkBox.getSelection());
				}
			}
		});
		return checkBox;
	}
	
	private Combo getSeverityCombo(Composite parent) {
		if(severityCombo == null) {
			severityCombo = new Combo(parent, SWT.READ_ONLY);
			for (int i = 0; i < fEntryNamesAndValues.length; i++) {
				severityCombo.add(fEntryNamesAndValues[i][0], i);
			}
			
			severityCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					String oldValue = fValue;
					fValue = severityCombo.getText();
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, fValue);
				}
			});
		}
		
		return severityCombo;
	}

	@Override
	protected void doLoad() {
		if(severityCombo != null) {
			String s = codanPreferencesLoader.getProperty(problemId);
			System.out.println("loading = " + s);
			if (s == null || s.length() == 0) {
				System.out.println("is == null");
				//s = getPreferenceStore().getString(problemId);
				//s = codanPreferencesLoader.getProperty(problemId);
				System.out.println(s);
			}
			codanPreferencesLoader.setProperty(problemId, s);
			updateUI();
		}
	}

	@Override
	protected void doLoadDefault() {
		System.out.println("doLoadDefault");
		if (severityCombo != null) {
			CodanPreferencesLoader defaultPreferences = new CodanPreferencesLoader(CodanRuntime.getInstance().getCheckersRegistry().getDefaultProfile());
			String s = getPreferenceStore().getDefaultString(problemId);
			if (s == null || s.length() == 0) {
				s = defaultPreferences.getProperty(problemId);
			}
			codanPreferencesLoader.setProperty(problemId, s);
			updateUI();
			setPresentsDefaultValue(true);
		}
	}

	@Override
	protected void doStore() {
		String val = enabledCheckBox.getSelection() ? fValue : ("-" + fValue);
		codanPreferencesLoader.setProperty(problemId, val);
		getPreferenceStore().setValue(problemId, val);
		String params = codanPreferencesLoader.getPreferencesString(problemId);
		System.out.println(params);
		if (params != null) {
			getPreferenceStore().setValue(codanPreferencesLoader.getPreferencesKey(problemId), params);
		}
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
	
	private void updateUI() {
		IProblem problem = getProblem();
		group.setText(problem.getName());
		enabledCheckBox.setSelection(problem.isEnabled());
		updateSeverityCombo(problem);
		updateLaunchModes(problem);
		updateEnabledState();
	}
	
	private void updateSeverityCombo(IProblem problem) {
		switch(problem.getSeverity()) {
		case Info:
			severityCombo.select(2);
			break;
		case Warning:
			severityCombo.select(1);
			break;
		case Error:
			severityCombo.select(0);
			break;
		}
		fValue = severityCombo.getText();
	}
	
	private void updateLaunchModes(IProblem problem) {
		System.out.println("updating the launch modes");
		IProblemPreference info = problem.getPreference();
		if(info instanceof RootProblemPreference) {
			System.out.println("is here");
			LaunchModeProblemPreference launchModes = ((RootProblemPreference) info).getLaunchModePreference();
			runAsYouType.setSelection(launchModes.isRunningInMode(CheckerLaunchMode.RUN_AS_YOU_TYPE));
			runOnFileOpen.setSelection(launchModes.isRunningInMode(CheckerLaunchMode.RUN_ON_FILE_OPEN));
			runOnFileSave.setSelection(launchModes.isRunningInMode(CheckerLaunchMode.RUN_ON_FILE_SAVE));
			runOnIncrementalBuild.setSelection(launchModes.isRunningInMode(CheckerLaunchMode.RUN_ON_INC_BUILD));
			runOnFullBuild.setSelection(launchModes.isRunningInMode(CheckerLaunchMode.RUN_ON_FULL_BUILD));
			runOnDemand.setSelection(launchModes.isRunningInMode(CheckerLaunchMode.RUN_ON_DEMAND));
		}
	}
	
	private void updateEnabledState() {
		boolean overrideWorkspaceSettings = getLabelControl(parent).isEnabled();
		boolean checkerEnabled = enabledCheckBox.getSelection();
		boolean enabled = overrideWorkspaceSettings && checkerEnabled;
		enabledCheckBox.setEnabled(overrideWorkspaceSettings);
		
		severityLabel.setEnabled(enabled);
		severityCombo.setEnabled(enabled);
		launchingLabel.setEnabled(enabled);
		runAsYouType.setEnabled(enabled);
		runOnFileOpen.setEnabled(enabled);
		runOnFileSave.setEnabled(enabled);
		runOnIncrementalBuild.setEnabled(enabled);
		runOnFullBuild.setEnabled(enabled);
		runOnDemand.setEnabled(enabled);
		
		Color labelColor = null;
		if(enabled) {
			labelColor = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
		} else {
			labelColor = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
		}
		
		severityLabel.setForeground(labelColor);
		launchingLabel.setForeground(labelColor);
	}
	
	private IProblem getProblem() {
		IProblem[] probs = codanPreferencesLoader.getProblems();
		for(IProblem prob : probs) {
			if(prob.getId().equals(problemId)) {
				return prob;
			}
		}
		return null;
	}
	
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		updateEnabledState();
	}
}
