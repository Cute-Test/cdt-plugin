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

import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard.NewSuiteFileCreationWizardPage.SourceFolderFieldAdapter;


// Copied and adapted from CUTE
@SuppressWarnings("restriction")
class StringButtonDialogField extends StringDialogField {

   private Button                         fBrowseButton;
   private String                         fBrowseButtonLabel;
   private final SourceFolderFieldAdapter fStringButtonAdapter;
   private final boolean                  fButtonEnabled;

   public StringButtonDialogField(SourceFolderFieldAdapter sourceFolderAdapter) {
      fStringButtonAdapter = sourceFolderAdapter;
      fBrowseButtonLabel = "!Browse...!";
      fButtonEnabled = true;
   }

   void setButtonLabel(String label) {
      fBrowseButtonLabel = label;
   }

   void changeControlPressed() {
      fStringButtonAdapter.changeControlPressed();
   }

   @Override
   public Control[] doFillIntoGrid(Composite parent, int nColumns) {
      assertEnoughColumns(nColumns);
      Button button = getChangeControl(parent);
      button.setLayoutData(gridDataForButton(button, 1));
      return new Control[] { getLabel(parent), getText(parent, nColumns - 2), button };
   }

   @Override
   public int getNumberOfControls() {
      return 3;
   }

   private static GridData gridDataForButton(Button button, int span) {
      GridData gd = new GridData();
      gd.horizontalAlignment = GridData.FILL;
      gd.grabExcessHorizontalSpace = false;
      gd.horizontalSpan = span;
      gd.widthHint = SWTUtil.getButtonWidthHint(button);
      return gd;
   }

   private Button getChangeControl(Composite parent) {
      if (fBrowseButton == null) {
         assertCompositeNotNull(parent);
         fBrowseButton = new Button(parent, SWT.PUSH);
         fBrowseButton.setText(fBrowseButtonLabel);
         fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
         fBrowseButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
               changeControlPressed();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
               changeControlPressed();
            }
         });

      }
      return fBrowseButton;
   }

   @Override
   protected void updateEnableState() {
      super.updateEnableState();
      if (isOkToUse(fBrowseButton)) {
         fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
      }
   }
}
