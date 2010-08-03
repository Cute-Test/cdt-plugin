/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.wizard.CuteVersionComposite;

/**
 * @author egraf
 * @since 4.0
 *
 */
public class ChangeCuteVersionWizardPage extends WizardPage {
	
	private Composite composite;
	private CuteVersionComposite cuteVersionComp;
	private ImageDescriptor imageDesc;

	protected ChangeCuteVersionWizardPage() {
		super("changeCuteVersionPage"); //$NON-NLS-1$
		imageDesc = UiPlugin.getImageDescriptor("cute_logo.png"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setTitle(Messages.getString("ChangeCuteVersionWizardPage.changeCuteVersion")); //$NON-NLS-1$

		cuteVersionComp = new CuteVersionComposite(composite);
		setControl(composite);
	}

	@Override
	public Image getImage() {
		return imageDesc.createImage();
	}

	public String getVersionString() {
		return cuteVersionComp.getVersionString();
	}
	
	

}
