/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
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
