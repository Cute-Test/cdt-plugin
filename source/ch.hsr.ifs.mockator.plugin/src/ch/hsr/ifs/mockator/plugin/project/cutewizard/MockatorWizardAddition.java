package ch.hsr.ifs.mockator.plugin.project.cutewizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


public class MockatorWizardAddition implements ICuteWizardAddition {

   private boolean withMockatorSupport;
   private Button  cpp03Button;
   private Button  cpp11Button;

   @Override
   public Control createComposite(Composite comp) {
      final Button check = createMockatorCheckBox(comp);
      createCppStdArea(comp);
      setDefaultCppStd();
      check.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            withMockatorSupport = check.getSelection();
            cpp03Button.setEnabled(withMockatorSupport);
            cpp11Button.setEnabled(withMockatorSupport);
         }
      });
      return comp;
   }

   private static Button createMockatorCheckBox(Composite comp) {
      Button check = new Button(comp, SWT.CHECK);
      check.setText(I18N.AddMockatorSupportToCUTEProject);
      return check;
   }

   private void createCppStdArea(Composite parent) {
      Composite comp = createRadioGroup(parent);
      createRadioButtons(comp);
   }

   private void setDefaultCppStd() {
      if (CppStandard.getDefaultCppStd() == CppStandard.fromName(cpp03Button.getText())) {
         cpp03Button.setSelection(true);
      } else {
         cpp11Button.setSelection(true);
      }
   }

   private void createRadioButtons(Composite comp) {
      cpp03Button = new Button(comp, SWT.RADIO);
      cpp03Button.setText(CppStandard.Cpp03Std.toString());
      cpp03Button.setEnabled(false);
      cpp11Button = new Button(comp, SWT.RADIO);
      cpp11Button.setText(CppStandard.Cpp11Std.toString());
      cpp11Button.setEnabled(false);
      cpp11Button.setSelection(true);
   }

   private static Composite createRadioGroup(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(2, false);
      comp.setLayout(layout);
      return comp;
   }

   private CppStandard getSelectedCppStd() {
      if (cpp03Button.getSelection()) return CppStandard.Cpp03Std;

      return CppStandard.Cpp11Std;
   }

   @Override
   public ICuteWizardAdditionHandler getHandler() {
      return new MockatorWizardAdditionHandler(withMockatorSupport, getSelectedCppStd());
   }
}
