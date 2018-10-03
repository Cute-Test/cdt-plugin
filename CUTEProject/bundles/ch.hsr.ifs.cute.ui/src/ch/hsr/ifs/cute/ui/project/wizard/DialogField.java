/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


/**
 * @since 4.0
 */
public class DialogField {

    private Label    fLabel;
    protected String fLabelText;

    private IDialogFieldListener fDialogFieldListener;

    private boolean fEnabled;

    public DialogField() {
        fEnabled = true;
        fLabel = null;
        fLabelText = "";
    }

    public void setLabelText(String labeltext) {
        fLabelText = labeltext;
    }

    // ------ change listener

    public final void setDialogFieldListener(IDialogFieldListener listener) {
        fDialogFieldListener = listener;
    }

    public void dialogFieldChanged() {
        if (fDialogFieldListener != null) {
            fDialogFieldListener.dialogFieldChanged(this);
        }
    }

    // ------- focus management

    /**
     * Tries to set the focus to the dialog field. Returns <code>true</code> if the dialog field can take focus. To be reimplemented by dialog field
     * implementors.
     */
    public boolean setFocus() {
        return false;
    }

    public void postSetFocusOnDialogField(Display display) {
        if (display != null) {
            display.asyncExec(() -> setFocus());
        }
    }

    // ------- layout helpers

    /**
     * Creates all controls of the dialog field and fills it to a composite. The composite is assumed to have <code>GridLayout</code> as layout. The
     * dialog field will adjust its controls' spans to the
     * number of columns given. To be reimplemented by dialog field implementors.
     */
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        assertEnoughColumns(nColumns);

        Label label = getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(nColumns));

        return new Control[] { label };
    }

    /**
     * Returns the number of columns of the dialog field. To be reimplemented by dialog field implementors.
     */
    public int getNumberOfControls() {
        return 1;
    }

    protected static GridData gridDataForLabel(int span) {
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = span;
        return gd;
    }

    // ------- ui creation

    public Label getLabelControl(Composite parent) {
        if (fLabel == null) {
            assertCompositeNotNull(parent);

            fLabel = new Label(parent, SWT.LEFT);
            fLabel.setFont(parent.getFont());
            fLabel.setEnabled(fEnabled);
            if (fLabelText != null && !"".equals(fLabelText)) {
                fLabel.setText(fLabelText);
            } else {
                // XXX: to avoid a 16 pixel wide empty label - revisit
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

    protected final boolean isOkToUse(Control control) {
        return (control != null) && !(control.isDisposed());
    }

    // --------- enable / disable management

    public final void setEnabled(boolean enabled) {
        if (enabled != fEnabled) {
            fEnabled = enabled;
            updateEnableState();
        }
    }

    /**
     * Called when the enable state changed. To be extended by dialog field implementors.
     */
    protected void updateEnableState() {
        if (fLabel != null) {
            fLabel.setEnabled(fEnabled);
        }
    }

    public final boolean isEnabled() {
        return fEnabled;
    }

    protected final void assertCompositeNotNull(Composite comp) {
        Assert.isNotNull(comp, "uncreated control requested with composite null");
    }

    protected final void assertEnoughColumns(int nColumns) {
        Assert.isTrue(nColumns >= getNumberOfControls(), "given number of columns is too small");
    }

}
