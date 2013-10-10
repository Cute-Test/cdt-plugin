/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.util.Vector;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Emanuel Graf
 * 
 */
public class LibReferencePage extends CuteVersionWizardPage implements ICheckStateListener {

	private CheckboxTableViewer listViewer;
	private Vector<IProject> libProjects;
	private final IWizardContainer wizardDialog;

	/**
	 * @since 4.0
	 */
	public LibReferencePage(IWizardPage nextPage, IWizardPage previousPage, IWizardContainer wc) {
		super(nextPage, previousPage, "ch.hsr.ifs.cutelauncher.ui.LibRefPage");
		wizardDialog = wc;
	}

	@Override
	protected boolean isCustomPageComplete() {
		if (getCheckedProjects().size() < 1) {
			return false;
		}
		return super.isCustomPageComplete();
	}

	@Override
	public String getName() {
		return Messages.getString("LibReferencePage.ReferenceToLib"); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		addLibProjectSelectionList();
	}

	private void addLibProjectSelectionList() {
		libProjects = getLibProjects();
		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.verticalIndent = 20;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		listViewer.setContentProvider(getContentProvider());
		listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(libProjects);
		listViewer.addCheckStateListener(this);
	}

	private IContentProvider getContentProvider() {
		return new IStructuredContentProvider() {

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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

	@Override
	public String getDescription() {
		return Messages.getString("LibReferencePage.ChooseLib"); //$NON-NLS-1$
	}

	boolean errorMessageFlag = false;

	@Override
	public String getErrorMessage() {
		return errorMessageFlag ? Messages.getString("LibReferencePage.SelectLib") : super.getErrorMessage();
	}

	@Override
	public String getTitle() {
		return Messages.getString("LibReferencePage.LibProjectTest"); //$NON-NLS-1$
	}

	public Vector<IProject> getCheckedProjects() {
		Vector<IProject> checkedProjects = new Vector<IProject>();
		if (listViewer == null) {
			return checkedProjects;
		}

		for (Object obj : listViewer.getCheckedElements()) {
			if (obj instanceof IProject) {
				checkedProjects.add((IProject) obj);
			}
		}
		return checkedProjects;
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		Vector<IProject> list = getCheckedProjects();
		errorMessageFlag = list.isEmpty();

		wizardDialog.updateMessage();
		wizardDialog.updateButtons();
	}

	private Vector<IProject> getLibProjects() {
		Vector<IProject> libProjects = new Vector<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			if (info != null) {
				IConfiguration[] configs = info.getManagedProject().getConfigurations();
				IBuildProperty artifactType = configs[0].getBuildProperties().getProperty(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID);
				if (artifactType != null) {
					String artifactTypeName = artifactType.getValue().getId();
					boolean isSharedLibProj = artifactTypeName.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB);
					boolean isStaticLibProj = artifactTypeName.equals(ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_STATICLIB);
					if (isSharedLibProj || isStaticLibProj) {
						libProjects.add(project);
					}
				}
			}
		}
		return libProjects;
	}
}
