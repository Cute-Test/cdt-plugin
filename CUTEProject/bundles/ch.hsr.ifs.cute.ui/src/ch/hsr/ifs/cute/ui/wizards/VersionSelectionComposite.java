/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.headers.ICuteHeaders;


/**
 * @author egraf
 * @since 4.0
 *
 */
public class VersionSelectionComposite extends Composite {

    private Combo combo;

    public VersionSelectionComposite(Composite parent, ICuteHeaders currentCuteHeaders) {
        super(parent, SWT.NULL);
        createCuteVersionCompsite(currentCuteHeaders);
    }

    public VersionSelectionComposite(Composite parent) {
        this(parent, null);
    }

    public String getVersionString() {
        return combo.getText();
    }

    public String getErrorMessage() {
        if (combo.getItems().length == 0) {
            return WizardMessages.NotInstalled;
        }
        return null;
    }

    public boolean isComplete() {
        return !combo.getText().isEmpty();
    }

    private void createCuteVersionCompsite(ICuteHeaders currentCuteHeaders) {
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(this, SWT.HORIZONTAL);
        label.setText(WizardMessages.VersionLabel);

        combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);

        int i = 0;
        // TODO(tstauber - Apr 16, 2018) This should only show compatible headers
        Iterator<ICuteHeaders> iter = ICuteHeaders.loadedHeaders().iterator();
        while (iter.hasNext()) {
            ICuteHeaders headers = iter.next();
            combo.add(headers.getVersionString());
            if (headers == currentCuteHeaders) combo.select(i);
            if (!iter.hasNext()) combo.select(i);
            i++;
        }

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        combo.setLayoutData(data);
    }

}
