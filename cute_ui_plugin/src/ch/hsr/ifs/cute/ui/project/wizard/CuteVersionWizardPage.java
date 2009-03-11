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
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.List;
import java.util.SortedSet;

import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 *
 */
public class CuteVersionWizardPage extends MBSCustomPage {
	
	private static final String CHOOSE_CUTE_VERSION = "Choose Cute Version";
	private Composite composite;
	private String description = CHOOSE_CUTE_VERSION;
	private ImageDescriptor imageDesc;
	private String title = CHOOSE_CUTE_VERSION;
	private CuteVersionComposite cuteComp;
	private IWizardPage startingPage;
	private CDTConfigWizardPage configPage;

	public CuteVersionWizardPage(IWizardPage startingPage,
			CDTConfigWizardPage configPage) {
		this.startingPage = startingPage;
		this.configPage = configPage;
	}


	@Override
	public IWizardPage getNextPage() {
		return configPage;
	}


	@Override
	public IWizardPage getPreviousPage() {
		return startingPage;
	}


	@Override
	protected boolean isCustomPageComplete() {
		return true;
	}


	public String getName() {
		return CHOOSE_CUTE_VERSION;
	}


	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		cuteComp = new CuteVersionComposite(composite);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		cuteComp.setLayoutData(data);
		
	}


	


	public void dispose() {
		composite.dispose();
	}


	public Control getControl() {
		return composite;
	}


	public String getDescription() {
		return description;
	}


	public String getErrorMessage() {
		return "";
	}


	public Image getImage() {
		return imageDesc.createImage();
	}


	public String getMessage() {
		// TODO Auto-generated method stub
		return CHOOSE_CUTE_VERSION;
	}


	public String getTitle() {
		return title;
	}


	public void performHelp() {
		// TODO Auto-generated method stub

	}


	public void setDescription(String description) {
		this.description = description;
	}


	public void setImageDescriptor(ImageDescriptor image) {
		imageDesc = image;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public void setVisible(boolean visible) {
		composite.setVisible(visible);

	}
	
	public String getCuteVersionString() {
		return cuteComp.getVersionString();
	}

}
