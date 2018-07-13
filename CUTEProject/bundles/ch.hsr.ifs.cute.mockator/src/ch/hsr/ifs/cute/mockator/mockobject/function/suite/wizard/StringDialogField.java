/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.mockator.mockobject.function.suite.wizard;

/*
 * (c) Copyright IBM Corp. 2000, 2001. All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// Copied and adapted from CUTE
class StringDialogField extends DialogField {

   private String         fText;
   private Text           fTextControl;
   private ModifyListener fModifyListener;

   public StringDialogField() {
      fText = "";
   }

   @Override
   public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
      assertEnoughColumns(nColumns);
      return new Control[] { getLabel(parent), getText(parent, nColumns - 1) };
   }

   protected Text getText(final Composite parent, final int nColumns) {
      final Text text = getTextControl(parent);
      text.setLayoutData(gridDataForText(nColumns));
      return text;
   }

   protected Label getLabel(final Composite parent) {
      final Label label = getLabelControl(parent);
      label.setLayoutData(gridDataForLabel(1));
      return label;
   }

   @Override
   public int getNumberOfControls() {
      return 2;
   }

   protected static GridData gridDataForText(final int span) {
      final GridData gd = new GridData();
      gd.horizontalAlignment = GridData.FILL;
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalSpan = span;
      return gd;
   }

   @Override
   public boolean setFocus() {
      if (isOkToUse(fTextControl)) {
         fTextControl.setFocus();
         fTextControl.setSelection(0, fTextControl.getText().length());
      }
      return true;
   }

   Text getTextControl(final Composite parent) {
      if (fTextControl == null) {
         assertCompositeNotNull(parent);
         fModifyListener = e -> doModifyText();

         fTextControl = new Text(parent, SWT.SINGLE | SWT.BORDER);
         fTextControl.setText(fText);
         fTextControl.setFont(parent.getFont());
         fTextControl.addModifyListener(fModifyListener);
         fTextControl.setEnabled(isEnabled());
      }
      return fTextControl;
   }

   private void doModifyText() {
      if (isOkToUse(fTextControl)) {
         fText = fTextControl.getText();
      }
      dialogFieldChanged();
   }

   @Override
   protected void updateEnableState() {
      super.updateEnableState();
      if (isOkToUse(fTextControl)) {
         fTextControl.setEnabled(isEnabled());
      }
   }

   String getText() {
      return fText;
   }

   void setTextWithoutUpdate(final String text) {
      fText = text;
      if (isOkToUse(fTextControl)) {
         fTextControl.removeModifyListener(fModifyListener);
         fTextControl.setText(text);
         fTextControl.addModifyListener(fModifyListener);
      }
   }
}
