/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard;

import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// Copied and adapted from CUTE
class SelectionButtonDialogField extends DialogField {

   private final int           fButtonStyle;
   private Button              fButton;
   private boolean             fIsSelected;
   private final DialogField[] fAttachedDialogFields;

   public SelectionButtonDialogField(final int buttonStyle) {
      fIsSelected = false;
      fAttachedDialogFields = null;
      fButtonStyle = buttonStyle;
   }

   @Override
   public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
      assertEnoughColumns(nColumns);
      final Button button = getSelectionButton(parent);
      final GridData gd = new GridData();
      gd.horizontalSpan = nColumns;
      gd.horizontalAlignment = GridData.FILL;

      if (fButtonStyle == SWT.PUSH) {
         gd.widthHint = SWTUtil.getButtonWidthHint(button);
      }

      button.setLayoutData(gd);
      return new Control[] { button };
   }

   @Override
   public int getNumberOfControls() {
      return 1;
   }

   Button getSelectionButton(final Composite group) {
      if (fButton == null) {
         assertCompositeNotNull(group);
         fButton = new Button(group, fButtonStyle);
         fButton.setFont(group.getFont());
         fButton.setText(fLabelText);
         fButton.setEnabled(isEnabled());
         fButton.setSelection(fIsSelected);
         fButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
               doWidgetSelected();
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
               doWidgetSelected();
            }
         });
      }
      return fButton;
   }

   private void doWidgetSelected() {
      if (isOkToUse(fButton)) {
         changeValue(fButton.getSelection());
      }
   }

   private void changeValue(final boolean newState) {
      if (fIsSelected != newState) {
         fIsSelected = newState;

         if (fAttachedDialogFields != null) {
            boolean focusSet = false;
            for (final DialogField fAttachedDialogField : fAttachedDialogFields) {
               fAttachedDialogField.setEnabled(fIsSelected);
               if (fIsSelected && !focusSet) {
                  focusSet = fAttachedDialogField.setFocus();
               }
            }
         }
         dialogFieldChanged();
      } else if (fButtonStyle == SWT.PUSH) {
         dialogFieldChanged();
      }
   }

   boolean isSelected() {
      return fIsSelected;
   }

   void setSelection(final boolean selected) {
      changeValue(selected);
      if (isOkToUse(fButton)) {
         fButton.setSelection(selected);
      }
   }

   @Override
   protected void updateEnableState() {
      super.updateEnableState();
      if (isOkToUse(fButton)) {
         fButton.setEnabled(isEnabled());
      }
   }
}
