/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.hsr.ifs.cute.ui.UiPlugin;

/**
 * @author Emanuel Graf
 * 
 */
public class CuteVersionWizardPage extends MBSCustomPage {

	private Composite composite;
	private final CDTConfigWizardPage configPage;

	private final IWizardPage startingWizardPage;
	private CuteVersionComposite cuteVersionComp;
	private ImageDescriptor imageDesc;
	private CuteWizardHandler handler;
	protected boolean enableGcov = false;

	public CuteVersionWizardPage(CDTConfigWizardPage configWizardPage,
			IWizardPage staringWizardPage, CuteWizardHandler cuteWizardHandler) {
		pageID = "ch.hsr.ifs.cutelauncher.ui.CuteVersionPage"; //$NON-NLS-1$
		this.configPage = configWizardPage;
		this.startingWizardPage = staringWizardPage;
		imageDesc = UiPlugin.getImageDescriptor("cute_logo.png"); //$NON-NLS-1$
		handler = cuteWizardHandler;
		
	}

	@Override
	protected boolean isCustomPageComplete() {
		return cuteVersionComp != null ? cuteVersionComp.isComplete() : false;
	}

	public String getName() {
		return Messages.getString("LibReferencePage.ReferenceToLib"); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		cuteVersionComp = new CuteVersionComposite(composite);
		
		IToolChain[] tcs = handler.getSelectedToolChains();
		if(tcs[0].getBaseId().contains("gnu")){ //$NON-NLS-1$

			final Button check = new Button(composite, SWT.CHECK);
			check.setText(Messages.getString("CuteVersionWizardPage.EnableGcov")); //$NON-NLS-1$
			check.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					enableGcov = check.getSelection();
				}

			});
		}
	}

	public void dispose() {
		composite.dispose();

	}

	public Control getControl() {
		return composite;
	}

	public String getDescription() {
		return new String(Messages.getString("CuteVersionWizardPage.ChooseCuteVersion")); //$NON-NLS-1$
	}

	public String getErrorMessage() {
		return cuteVersionComp.getErrorMessage();
	}

	public Image getImage() {
		return imageDesc.createImage();
	}

	public String getMessage() {
		return null;
	}

	public String getTitle() {
		return new String(Messages.getString("CuteVersionWizardPage.CuteVersion")); //$NON-NLS-1$
	}

	public void performHelp() {
		// do nothing

	}

	public void setDescription(String description) {
		// do nothing

	}

	public void setTitle(String title) {
		// do nothing

	}

	public void setVisible(boolean visible) {
		composite.setVisible(visible);

	}

	@Override
	public IWizardPage getNextPage() {
		return configPage;
	}

	@Override
	public IWizardPage getPreviousPage() {
		return startingWizardPage;
	}

	public String getCuteVersionString() {
		return cuteVersionComp.getVersionString();
	}

	public void setImageDescriptor(ImageDescriptor image) {
		// do nothing
	}

}
