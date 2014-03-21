package ch.hsr.ifs.cute.macronator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class Settings extends PropertyPage {

    private Text textField;

    @Override
    protected Control createContents(Composite parent) {

        // create settings group
        Group group = new Group(parent, SWT.NONE);
        group.setText("Settings");
        group.setLayout(new GridLayout(2, false));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        group.setLayoutData(gridData);

        // create textfield label
        Label label = new Label(group, SWT.NONE);
        label.setText("store suppressed macros in: ");
        GridData labelGridData = new GridData(GridData.BEGINNING);
        labelGridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        // create settings
        textField = new Text(group, SWT.SINGLE | SWT.BORDER);
        GridData textFieldGridData = new GridData(GridData.FILL_HORIZONTAL);
        labelGridData.horizontalSpan = 1;
        textField.setLayoutData(textFieldGridData);
        textField.setText(getSuppressedMacroPreferenceValue());

        return group;
    }

    @Override
    public Point computeSize() {
        return super.computeSize();
    }

    @Override
    public boolean performOk() {
        setSuppressedMacroPreference();
        return true;
    }

    private void setSuppressedMacroPreference() {
        try {
            IProject project = ((IProject) getElement());
            project.setPersistentProperty(MacronatorPlugin.SUPPRESSED_MACROS, textField.getText());
        } catch (CoreException e) {
            MacronatorPlugin.log(e, "error saving settings");
        }
    }

    @Override
    protected void performDefaults() {
        textField.setText(MacronatorPlugin.getDefaultPreferenceValue(MacronatorPlugin.SUPPRESSED_MACROS));
        performOk();
    }

    private String getSuppressedMacroPreferenceValue() {
        try {
            String value = ((IProject) getElement()).getPersistentProperty(MacronatorPlugin.SUPPRESSED_MACROS);
            return value == null ? MacronatorPlugin.getDefaultPreferenceValue(MacronatorPlugin.SUPPRESSED_MACROS) : value;
        } catch (CoreException e) {
            MacronatorPlugin.log(e, "error loading property SUPPRESSED_MACROS; using default value");
            return MacronatorPlugin.getDefaultPreferenceValue(MacronatorPlugin.SUPPRESSED_MACROS);
        }
    }
}
