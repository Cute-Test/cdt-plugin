/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;
import ch.hsr.ifs.cute.ui.project.wizard.Messages;

/**
 * @author Emanuel Graf IFS
 *
 */
public class GcovWizardAddition implements ICuteWizardAddition {
	
	boolean enableGcov = false;

	public void createComposite(Composite comp) {
		final Button check = new Button(comp, SWT.CHECK);
		check.setText(Messages.getString("CuteVersionWizardPage.EnableGcov")); //$NON-NLS-1$
		check.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableGcov = check.getSelection();
			}

		});
	}

	public ICuteWizardAdditionHandler getHandler() {
		return new GcovAdditionHandler(this);
	}

}
