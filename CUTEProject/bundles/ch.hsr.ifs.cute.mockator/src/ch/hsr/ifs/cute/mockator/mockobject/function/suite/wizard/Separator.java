/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.mockator.mockobject.function.suite.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


// Copied and adapted from CUTE
class Separator extends DialogField {

    private Label     fSeparator;
    private final int fStyle;

    public Separator() {
        this(SWT.NONE);
    }

    public Separator(final int style) {
        super();
        fStyle = style;
    }

    Control[] doFillIntoGrid(final Composite parent, final int nColumns, final int height) {
        assertEnoughColumns(nColumns);
        final Control separator = getSeparator(parent);
        separator.setLayoutData(gridDataForSeperator(nColumns, height));
        return new Control[] { separator };
    }

    @Override
    public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
        return doFillIntoGrid(parent, nColumns, 4);
    }

    @Override
    public int getNumberOfControls() {
        return 1;
    }

    private static GridData gridDataForSeperator(final int span, final int height) {
        final GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.heightHint = height;
        gd.horizontalSpan = span;
        return gd;
    }

    private Control getSeparator(final Composite parent) {
        if (fSeparator == null) {
            assertCompositeNotNull(parent);
            fSeparator = new Label(parent, fStyle);
        }
        return fSeparator;
    }
}
