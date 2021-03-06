/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * Dialog field describing a separator.
 *
 * @since 4.0
 */
public class SeparatorField extends DialogField {

    private Label     fSeparator;
    private final int fStyle;

    public SeparatorField() {
        this(SWT.NONE);
    }

    public SeparatorField(int style) {
        super();
        fStyle = style;
    }

    // ------- layout helpers

    public Control[] doFillIntoGrid(Composite parent, int nColumns, int height) {
        assertEnoughColumns(nColumns);

        Control separator = getSeparator(parent);
        separator.setLayoutData(gridDataForSeperator(nColumns, height));

        return new Control[] { separator };
    }

    @Override
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        return doFillIntoGrid(parent, nColumns, 4);
    }

    @Override
    public int getNumberOfControls() {
        return 1;
    }

    protected static GridData gridDataForSeperator(int span, int height) {
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.heightHint = height;
        gd.horizontalSpan = span;
        return gd;
    }

    // ------- ui creation

    public Control getSeparator(Composite parent) {
        if (fSeparator == null) {
            assertCompositeNotNull(parent);
            fSeparator = new Label(parent, fStyle);
        }
        return fSeparator;
    }

}
