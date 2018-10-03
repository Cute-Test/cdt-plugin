/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.boost;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;


/**
 * @author Emanuel Graf IFS
 *
 */
public class BoostWizardAddition implements ICuteWizardAddition {

    boolean copyBoost;

    @Override
    public Control createComposite(Composite parent) {
        final Button check = new Button(parent, SWT.CHECK);
        check.setText(Messages.BoostWizardAddition_0);
        check.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                copyBoost = check.getSelection();
            }
        });
        return check;
    }

    @Override
    public ICuteWizardAdditionHandler getHandler() {
        return new BoostHandler(this);
    }
}
