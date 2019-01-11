/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Dialog Field containing a single button such as a radio or checkbox button.
 *
 * @since 4.0
 */

public class SelectionButtonDialogField extends DialogField {

    private Button        fButton;
    private boolean       fIsSelected;
    private DialogField[] fAttachedDialogFields;
    private final int     fButtonStyle;

    /**
     * Creates a selection button. Allowed button styles: SWT.RADIO, SWT.CHECK, SWT.TOGGLE, SWT.PUSH
     */
    public SelectionButtonDialogField(int buttonStyle) {
        super();
        fIsSelected = false;
        fAttachedDialogFields = null;
        fButtonStyle = buttonStyle;
    }

    /**
     * Attaches fields to the selection state of the selection button. The attached fields will be disabled if the selection button is not selected.
     */
    public void attachDialogFields(DialogField... dialogFields) {
        fAttachedDialogFields = dialogFields;
        for (DialogField curField : dialogFields) {
            curField.setEnabled(fIsSelected);
        }
    }

    public boolean isAttached(DialogField editor) {
        if (fAttachedDialogFields != null) {
            for (DialogField curField : fAttachedDialogFields) {
                if (curField.equals(editor)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------- layout helpers

    @Override
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        assertEnoughColumns(nColumns);

        Button button = getSelectionButton(parent);
        GridData gd = new GridData();
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

    // ------- ui creation

    /**
     * Returns the selection button widget. When called the first time, the widget will be created.
     *
     * @param group
     * the parent composite when called the first time, or <code>null</code> after.
     */
    public Button getSelectionButton(Composite group) {
        if (fButton == null) {
            assertCompositeNotNull(group);

            fButton = new Button(group, fButtonStyle);
            fButton.setFont(group.getFont());
            fButton.setText(fLabelText);
            fButton.setEnabled(isEnabled());
            fButton.setSelection(fIsSelected);
            fButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }
            });
        }
        return fButton;
    }

    protected void doWidgetSelected(SelectionEvent e) {
        if (isOkToUse(fButton)) {
            changeValue(fButton.getSelection());
        }
    }

    private void changeValue(boolean newState) {
        if (fIsSelected != newState) {
            fIsSelected = newState;
            if (fAttachedDialogFields != null) {
                boolean focusSet = false;
                for (DialogField fAttachedDialogField : fAttachedDialogFields) {
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

    // ------ model access

    public boolean isSelected() {
        return fIsSelected;
    }

    public void setSelection(boolean selected) {
        changeValue(selected);
        if (isOkToUse(fButton)) {
            fButton.setSelection(selected);
        }
    }

    // ------ enable / disable management

    @Override
    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fButton)) {
            fButton.setEnabled(isEnabled());
        }
    }

}
