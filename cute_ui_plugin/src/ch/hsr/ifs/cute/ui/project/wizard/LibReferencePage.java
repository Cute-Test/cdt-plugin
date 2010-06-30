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

import java.util.Vector;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizardContainer;
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
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ch.hsr.ifs.cute.ui.UiPlugin;

/**
 * @author Emanuel Graf
 *
 */
public class LibReferencePage extends MBSCustomPage implements ICheckStateListener{
	
	private Composite composite;
	private final CDTConfigWizardPage configPage;

    private CheckboxTableViewer listViewer;
	private Vector<IProject> libProjects;
	private final IWizardPage startingWizardPage;
	private final IWizardContainer wizardDialog;
	private CuteVersionComposite cuteVersionComp;
	private ImageDescriptor imageDesc;
	private CuteWizardHandler handler;
	protected boolean enableGcov = false;
	
	public LibReferencePage(CDTConfigWizardPage configWizardPage, IWizardPage staringWizardPage, IWizardContainer wc, CuteWizardHandler cuteWizardHandler) {
		pageID = "ch.hsr.ifs.cutelauncher.ui.LibRefPage"; //$NON-NLS-1$
		this.configPage = configWizardPage;
		this.startingWizardPage = staringWizardPage;
		this.handler = cuteWizardHandler;
		wizardDialog=wc;
		imageDesc = UiPlugin.getImageDescriptor("cute_logo.png"); //$NON-NLS-1$
	}

	@Override
	protected boolean isCustomPageComplete() {
		if (getCheckedProjects().size() < 1) {
			return false;
		}
		if (!cuteVersionComp.isComplete()) {
			return false;
		}
		return true;
	}

	public String getName() {
		return Messages.getString("LibReferencePage.ReferenceToLib"); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		libProjects = getLibProjects();
		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		cuteVersionComp = new CuteVersionComposite(composite);
		IToolChain[] tcs = handler.getSelectedToolChains();
		if(tcs[0].getBaseId().contains("gnu")){

			final Button check = new Button(composite, SWT.CHECK);
			check.setText("Enable gcov");
			check.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					setEnableGcov(check.getSelection());
				}

			});
		}

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP
                | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        data.verticalIndent = 20;
        listViewer.getTable().setLayoutData(data);
        
        listViewer.setLabelProvider(WorkbenchLabelProvider
                .getDecoratingWorkbenchLabelProvider());
        listViewer.setContentProvider(getContentProvider());
        listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(libProjects);
        listViewer.addCheckStateListener(this);
	}
	
	private IContentProvider getContentProvider() {
		return new IStructuredContentProvider() {

			
			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			@SuppressWarnings({ "rawtypes" })
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Vector) {
					Vector vec = (Vector) inputElement;
					return vec.toArray();
				}
				return null;
			}
			
		};
	}

	private Vector<IProject> getLibProjects() {
		Vector<IProject> libProjects = new Vector<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			if(info != null) {
				IConfiguration[] configs = info.getManagedProject().getConfigurations();
				IBuildProperty artifactType = configs[0].getBuildProperties().getProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID);
				if(artifactType != null) {
					String artifactTypeName = artifactType.getValue().getId();
					if(artifactTypeName.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB)||
							artifactTypeName.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB)) {
						libProjects.add(project);
					}
				}
			}
		}
		return libProjects;
	}

	public void dispose()
	{
		composite.dispose();

	}

	public Control getControl()
	{
		return composite;
	}

	public String getDescription()
	{
		return Messages.getString("LibReferencePage.ChooseLib"); //$NON-NLS-1$
	}

	boolean errorMessageFlag=false;
	public String getErrorMessage()
	{
		if(errorMessageFlag)return Messages.getString("LibReferencePage.SelectLib"); //$NON-NLS-1$
		return cuteVersionComp.getErrorMessage();
	}

	public Image getImage()
	{
//		return wizard.getDefaultPageImage();
		return imageDesc.createImage();
	}

	public String getMessage()
	{
		return null;
	}

	public String getTitle()
	{
		return Messages.getString("LibReferencePage.LibProjectTest"); //$NON-NLS-1$
	}

	public void performHelp()
	{
		// do nothing

	}

	public void setDescription(String description)
	{
		// do nothing

	}


	public void setTitle(String title)
	{
		// do nothing

	}

	public void setVisible(boolean visible)
	{
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

	public Vector<IProject> getCheckedProjects() {
		Vector<IProject> checkedProjects = new Vector<IProject>();
		if(listViewer==null)return checkedProjects;
		
		for(Object obj :listViewer.getCheckedElements()) {
			if (obj instanceof IProject) {
				checkedProjects.add((IProject) obj);
				
			}
		}
		return checkedProjects;
	}
	
	public String getCuteVersionString() {
		return cuteVersionComp.getVersionString();
	}
	
	public void checkStateChanged(CheckStateChangedEvent event){
		Vector<IProject> list=getCheckedProjects();
		if(list.size()<1)errorMessageFlag=true;
		else errorMessageFlag=false;
		
		wizardDialog.updateMessage();
		wizardDialog.updateButtons();
	}

	public void setImageDescriptor(ImageDescriptor image) {
		// do nothing
	}

	public void setEnableGcov(boolean enableGcov) {
		this.enableGcov = enableGcov;
	}

	public boolean isEnableGcov() {
		return enableGcov;
	}

}
