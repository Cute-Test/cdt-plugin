/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @since 4.0
 */
public class StringDialogField extends DialogField {
		
	private String fText;
	private Text fTextControl;
	private ModifyListener fModifyListener;
	
	public StringDialogField() {
		super();
		fText= ""; //$NON-NLS-1$
	}
			
	// ------- layout helpers
		
	/*
	 * @see DialogField#doFillIntoGrid
	 */
	@Override
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Text text= getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 1));
		
		return new Control[] { label, text };
	} 

	/*
	 * @see DialogField#getNumberOfControls
	 */
	@Override
	public int getNumberOfControls() {
		return 2;	
	}
	
	protected static GridData gridDataForText(int span) {
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= span;
		return gd;
	}	
	
	// ------- focus methods
	
	/*
	 * @see DialogField#setFocus
	 */
	@Override
	public boolean setFocus() {
		if (isOkToUse(fTextControl)) {
			fTextControl.setFocus();
			fTextControl.setSelection(0, fTextControl.getText().length());
		}
		return true;
	}
		
	// ------- ui creation			

	/**
	 * Creates or returns the created text control.
	 * @param parent The parent composite or <code>null</code> when the widget has
	 * already been created.
	 */		
	public Text getTextControl(Composite parent) {
		if (fTextControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener= new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};
			
			fTextControl= new Text(parent, SWT.SINGLE | SWT.BORDER);
			// moved up due to 1GEUNW2
			fTextControl.setText(fText);
			fTextControl.setFont(parent.getFont());
			fTextControl.addModifyListener(fModifyListener);
			
			fTextControl.setEnabled(isEnabled());
		}
		return fTextControl;
	}
	
	protected void doModifyText(ModifyEvent e) {
		if (isOkToUse(fTextControl)) {
			fText= fTextControl.getText();
		}
		dialogFieldChanged();
	}		
	
	// ------ enable / disable management
	
	/*
	 * @see DialogField#updateEnableState
	 */		
	@Override
	protected void updateEnableState() {
		super.updateEnableState();		
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(isEnabled());
		}	
	}		
		
	// ------ text access 
	
	/**
	 * Gets the text. Can not be <code>null</code>
	 */	
	public String getText() {
		return fText;
	}
	
	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	public void setText(String text) {
		fText= text;
		if (isOkToUse(fTextControl)) {
			fTextControl.setText(text);
		} else {
			dialogFieldChanged();
		}	
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	public void setTextWithoutUpdate(String text) {
		fText= text;
		if (isOkToUse(fTextControl)) {
			fTextControl.removeModifyListener(fModifyListener);
			fTextControl.setText(text);
			fTextControl.addModifyListener(fModifyListener);
		}
	}
	
}