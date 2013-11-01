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

import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;

/**
 * @author Emanuel Graf
 * @since 4.0
 * 
 */
public class NewCuteProjectWizardPage extends MBSCustomPage {

	protected static final int GRID_WIDTH = 2;
	protected Composite composite;
	private final IWizardPage nextPage;
	private final IWizardPage previousPage;
	private CuteVersionComposite cuteVersionComp;
	private final ImageDescriptor imageDesc;
	private ArrayList<ICuteWizardAddition> additions;

	public NewCuteProjectWizardPage(IWizardPage nextPage, IWizardPage previousPage, String pageId) {
		super(pageId);
		this.nextPage = nextPage;
		this.previousPage = previousPage;
		imageDesc = CuteUIPlugin.getImageDescriptor("cute_logo.png");
	}

	public NewCuteProjectWizardPage(IWizardPage nextPage, IWizardPage previousPage) {
		this(nextPage, previousPage, "ch.hsr.ifs.cutelauncher.ui.CuteVersionPage");
	}

	@Override
	protected boolean isCustomPageComplete() {
		return cuteVersionComp != null ? cuteVersionComp.isComplete() : !CuteUIPlugin.getInstalledCuteHeaders().isEmpty();
	}

	public String getName() {
		return Messages.getString("LibReferencePage.ReferenceToLib");
	}

	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.FILL);
		composite.setLayout(new GridLayout(GRID_WIDTH, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addCuteHeaderVersionSelectionDropdown();
		addWizardPageAdditions();
	}

	private void addCuteHeaderVersionSelectionDropdown() {
		cuteVersionComp = new CuteVersionComposite(composite);
		GridData gridData = new GridData();
		gridData.horizontalSpan = GRID_WIDTH;
		cuteVersionComp.setLayoutData(gridData);
	}

	private void addWizardPageAdditions() {
		for (ICuteWizardAddition addition : getAdditions()) {
			Control newChild = addition.createComposite(composite);
			GridData gridData = new GridData();
			gridData.horizontalSpan = GRID_WIDTH;
			newChild.setLayoutData(gridData);
		}
	}

	public void dispose() {
		composite.dispose();
	}

	public Control getControl() {
		return composite;
	}

	public String getDescription() {
		return Messages.getString("CuteVersionWizardPage.CuteProjectPageDescription");
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
		return Messages.getString("CuteVersionWizardPage.CuteVersion");
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
		return nextPage;
	}

	@Override
	public IWizardPage getPreviousPage() {
		return previousPage;
	}

	public String getCuteVersionString() {
		if (cuteVersionComp != null) {
			return cuteVersionComp.getVersionString();
		} else {
			return CuteUIPlugin.getInstalledCuteHeaders().first().getVersionString();
		}
	}

	public void setImageDescriptor(ImageDescriptor image) {
		// do nothing
	}

	public List<ICuteWizardAddition> getAdditions() {
		if (additions == null) {
			additions = new ArrayList<ICuteWizardAddition>();
			try {
				IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CuteUIPlugin.PLUGIN_ID, "wizardAddition");
				if (extension != null) {
					IExtension[] extensions = extension.getExtensions();
					for (IExtension extension2 : extensions) {
						IConfigurationElement[] configElements = extension2.getConfigurationElements();
						String className = configElements[0].getAttribute("compositeProvider");
						Object newInstance = ((Class<?>) Platform.getBundle(extension2.getContributor().getName()).loadClass(className))
								.newInstance();
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
