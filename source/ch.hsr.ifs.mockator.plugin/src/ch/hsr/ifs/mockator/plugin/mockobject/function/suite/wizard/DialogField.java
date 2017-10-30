/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard.NewSuiteFileCreationWizardPage.SourceFolderFieldAdapter;


// Copied and adapted from CUTE
class DialogField {

   protected String                 fLabelText;
   private Label                    fLabel;
   private SourceFolderFieldAdapter fDialogFieldListener;
   private boolean                  fEnabled;

   public DialogField() {
      fEnabled = true;
      fLabel = null;
      fLabelText = "";
   }

   public void setLabelText(String labeltext) {
      fLabelText = labeltext;
   }

   public final void setDialogFieldListener(SourceFolderFieldAdapter sourceFolderAdapter) {
      fDialogFieldListener = sourceFolderAdapter;
   }

   public void dialogFieldChanged() {
      if (fDialogFieldListener != null) {
         fDialogFieldListener.dialogFieldChanged();
      }
   }

   public boolean setFocus() {
      return false;
   }

   public void postSetFocusOnDialogField(Display display) {
      if (display != null) {
         display.asyncExec(new Runnable() {

            @Override
            public void run() {
               setFocus();
            }
         });
      }
   }

   public Control[] doFillIntoGrid(Composite parent, int nColumns) {
      assertEnoughColumns(nColumns);
      Label label = getLabelControl(parent);
      label.setLayoutData(gridDataForLabel(nColumns));
      return new Control[] { label };
   }

   public int getNumberOfControls() {
      return 1;
   }

   protected static GridData gridDataForLabel(int span) {
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      gd.horizontalSpan = span;
      return gd;
   }

   public Label getLabelControl(Composite parent) {
      if (fLabel == null) {
         assertCompositeNotNull(parent);

         fLabel = new Label(parent, SWT.LEFT);
         fLabel.setFont(parent.getFont());
         fLabel.setEnabled(fEnabled);
         if (fLabelText != null && !"".equals(fLabelText)) {
            fLabel.setText(fLabelText);
         } else {
            fLabel.setText(".");
            fLabel.setVisible(false);
         }
      }
      return fLabel;
   }

   public static Control createEmptySpace(Composite parent) {
      return createEmptySpace(parent, 1);
   }

   public static Control createEmptySpace(Composite parent, int span) {
      Label label = new Label(parent, SWT.LEFT);
      GridData gd = new GridData();
      gd.horizontalAlignment = GridData.BEGINNING;
      gd.grabExcessHorizontalSpace = false;
      gd.horizontalSpan = span;
      gd.horizontalIndent = 0;
      gd.widthHint = 0;
      gd.heightHint = 0;
      label.setLayoutData(gd);
      return label;
   }

   protected static boolean isOkToUse(Control control) {
      return (control != null) && !(control.isDisposed());
   }

   public final void setEnabled(boolean enabled) {
      if (enabled != fEnabled) {
         fEnabled = enabled;
         updateEnableState();
      }
   }

   protected void updateEnableState() {
      if (fLabel != null) {
         fLabel.setEnabled(fEnabled);
      }
   }

   public final boolean isEnabled() {
      return fEnabled;
   }

   protected static void assertCompositeNotNull(Composite comp) {
      Assert.notNull(comp, "uncreated control requested with composite null");
   }

   protected final void assertEnoughColumns(int nColumns) {
      Assert.isTrue(nColumns >= getNumberOfControls(), "given number of columns is too small");
   }
}
