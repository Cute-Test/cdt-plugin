/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.UiPlugin;

/**
 * @author Emanuel Graf
 * @since 4.0
 * 
 */
public class CuteVersionWizardPage extends MBSCustomPage {

	protected Composite composite;
	private final CDTConfigWizardPage configPage;

	private final IWizardPage startingWizardPage;
	private CuteVersionComposite cuteVersionComp;
	private ImageDescriptor imageDesc;
	private ArrayList<ICuteWizardAddition> additions;

	public CuteVersionWizardPage(CDTConfigWizardPage configWizardPage,
			IWizardPage staringWizardPage) {
		pageID = "ch.hsr.ifs.cutelauncher.ui.CuteVersionPage"; //$NON-NLS-1$
		this.configPage = configWizardPage;
		this.startingWizardPage = staringWizardPage;
		imageDesc = UiPlugin.getImageDescriptor("cute_logo.png"); //$NON-NLS-1$
		
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
		for (ICuteWizardAddition addition : getAdditions()) {
			addition.createComposite(composite);
		}
		
	}

	public void dispose() {
		composite.dispose();

	}

	public Control getControl() {
		return composite;
	}

	public String getDescription() {
		return Messages.getString("CuteVersionWizardPage.ChooseCuteVersion"); //$NON-NLS-1$
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
		return Messages.getString("CuteVersionWizardPage.CuteVersion"); //$NON-NLS-1$
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

	public List<ICuteWizardAddition> getAdditions() {
		if(additions == null) {
			additions = new ArrayList<ICuteWizardAddition>();
			try{
				IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(UiPlugin.PLUGIN_ID, "wizardAddition"); //$NON-NLS-1$
				if (extension != null) {
					IExtension[] extensions = extension.getExtensions();
					for (IExtension extension2 : extensions) {
						IConfigurationElement[] configElements = extension2.getConfigurationElements();
						String className =configElements[0].getAttribute("compositeProvider"); //$NON-NLS-1$
						Object newInstance = ((Class<?>) Platform.getBundle(extension2.getContributor().getName()).loadClass(className)).newInstance();
						additions.add((ICuteWizardAddition) newInstance);
					}
				}
			} catch (ClassNotFoundException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		return additions;
	}

}