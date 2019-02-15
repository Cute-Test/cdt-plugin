package ch.hsr.ifs.cute.mockator.project.cutewizard;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;


public class MockatorWizardAddition implements ICuteWizardAddition {

    private boolean withMockatorSupport;
    private Button  cpp03Button;
    private Button  cpp11Button;

    @Override
    public Control createComposite(final Composite comp) {
        final Button check = createMockatorCheckBox(comp);
        createCppStdArea(comp);
        setDefaultCppStd();
        check.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                withMockatorSupport = check.getSelection();
                cpp03Button.setEnabled(withMockatorSupport);
                cpp11Button.setEnabled(withMockatorSupport);
            }
        });
        return comp;
    }

    private static Button createMockatorCheckBox(final Composite comp) {
        final Button check = new Button(comp, SWT.CHECK);
        check.setText(I18N.AddMockatorSupportToCUTEProject);
        return check;
    }

    private void createCppStdArea(final Composite parent) {
        final Composite comp = createRadioGroup(parent);
        createRadioButtons(comp);
    }

    private void setDefaultCppStd() {
        if (CppStandard.getDefaultCppStd() == CppStandard.fromName(cpp03Button.getText())) {
            cpp03Button.setSelection(true);
        } else {
            cpp11Button.setSelection(true);
        }
    }

    private void createRadioButtons(final Composite comp) {
        cpp03Button = new Button(comp, SWT.RADIO);
        cpp03Button.setText(CppStandard.Cpp03Std.toString());
        cpp03Button.setEnabled(false);
        cpp11Button = new Button(comp, SWT.RADIO);
        cpp11Button.setText(CppStandard.Cpp11Std.toString());
        cpp11Button.setEnabled(false);
        cpp11Button.setSelection(true);
    }

    private static Composite createRadioGroup(final Composite parent) {
        final Composite comp = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout(2, false);
        comp.setLayout(layout);
        return comp;
    }

    private CppStandard getSelectedCppStd() {
        return Optional.ofNullable(cpp03Button)
            .map(b -> b.getSelection() ? CppStandard.Cpp03Std : CppStandard.Cpp11Std)
            .orElse(CppStandard.Cpp11Std);
    }

    @Override
    public ICuteWizardAdditionHandler getHandler() {
        return new MockatorWizardAdditionHandler(withMockatorSupport, getSelectedCppStd());
    }
}
