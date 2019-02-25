/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.fields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * Dialog field containing a label and a combo control.
 *
 * @since 4.0
 */
public class LabeledComboField extends DialogField {

    private String         fText;
    private int            fSelectionIndex;
    private String[]       fItems;
    private Combo          fComboControl;
    private ModifyListener fModifyListener;
    private final int      fFlags;

    public LabeledComboField(int flags) {
        super();
        fText = "";
        fItems = new String[0];
        fFlags = flags;
        fSelectionIndex = -1;
    }

    // ------- layout helpers

    @Override
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        assertEnoughColumns(nColumns);

        Label label = getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(1));
        Combo combo = getComboControl(parent);
        combo.setLayoutData(gridDataForCombo(nColumns - 1));

        return new Control[] { label, combo };
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    protected static GridData gridDataForCombo(int span) {
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        return gd;
    }

    // ------- focus methods

    @Override
    public boolean setFocus() {
        if (isOkToUse(fComboControl)) {
            fComboControl.setFocus();
        }
        return true;
    }

    // ------- ui creation

    public Combo getComboControl(Composite parent) {
        if (fComboControl == null) {
            assertCompositeNotNull(parent);
            fModifyListener = e -> doModifyText(e);
            SelectionListener selectionListener = new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    doSelectionChanged(e);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {}
            };

            fComboControl = new Combo(parent, fFlags);
            // moved up due to 1GEUNW2
            fComboControl.setItems(fItems);
            if (fSelectionIndex != -1) {
                fComboControl.select(fSelectionIndex);
            } else {
                fComboControl.setText(fText);
            }
            fComboControl.setFont(parent.getFont());
            fComboControl.addModifyListener(fModifyListener);
            fComboControl.addSelectionListener(selectionListener);
            fComboControl.setEnabled(isEnabled());
        }
        return fComboControl;
    }

    protected void doModifyText(ModifyEvent e) {
        if (isOkToUse(fComboControl)) {
            fText = fComboControl.getText();
            fSelectionIndex = fComboControl.getSelectionIndex();
        }
        dialogFieldChanged();
    }

    protected void doSelectionChanged(SelectionEvent e) {
        if (isOkToUse(fComboControl)) {
            fItems = fComboControl.getItems();
            fText = fComboControl.getText();
            fSelectionIndex = fComboControl.getSelectionIndex();
        }
        dialogFieldChanged();
    }

    // ------ enable / disable management

    @Override
    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fComboControl)) {
            fComboControl.setEnabled(isEnabled());
        }
    }

    // ------ text access

    public String[] getItems() {
        return fItems;
    }

    public void setItems(String[] items) {
        fItems = items;
        if (isOkToUse(fComboControl)) {
            fComboControl.setItems(items);
        }
        dialogFieldChanged();
    }

    public String getText() {
        return fText;
    }

    public void setText(String text) {
        fText = text;
        if (isOkToUse(fComboControl)) {
            fComboControl.setText(text);
        } else {
            dialogFieldChanged();
        }
    }

    public void selectItem(int index) {
        if (isOkToUse(fComboControl)) {
            fComboControl.select(index);
        } else {
            if (index >= 0 && index < fItems.length) {
                fText = fItems[index];
                fSelectionIndex = index;
            }
        }
        dialogFieldChanged();
    }

    public int getSelectionIndex() {
        return fSelectionIndex;
    }

    public void setTextWithoutUpdate(String text) {
        fText = text;
        if (isOkToUse(fComboControl)) {
            fComboControl.removeModifyListener(fModifyListener);
            fComboControl.setText(text);
            fComboControl.addModifyListener(fModifyListener);
        }
    }

}
