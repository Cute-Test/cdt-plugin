/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewSuiteFileCreationWizard extends Wizard implements INewWizard {

	private NewSuiteFileCreationWizardPage page = null;
	private IStructuredSelection selection;

	public NewSuiteFileCreationWizard() {
		super();
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_SOURCEFILE);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setWindowTitle("New CUTE Suite File"); //$NON-NLS-1$
	}

	/*
	 * @see Wizard#createPages
	 */
	@Override
	public void addPages() {
		super.addPages();
		page = new NewSuiteFileCreationWizardPage();
		addPage(page);
		page.init(getSelection());
	}

	/**
	 * @since 4.0
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		selection = currentSelection;
	}

	private IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * @since 4.0
	 */
	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot(); // look all by default
	}

	@Override
	public boolean performFinish() {
		IWorkspaceRunnable op = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
				page.createFile(monitor);
			}
		};
		try {
			getContainer().run(true, true, new WorkbenchRunnableAdapter(op, getSchedulingRule()));
		} catch (InvocationTargetException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		//            //TODO need prefs option for opening editor
		//            boolean openInEditor = true;
		//            
		//			ITranslationUnit headerTU = fPage.getCreatedFileTU();
		//			if (headerTU != null) {
		//				IResource resource= headerTU.getResource();
		//				selectAndReveal(resource);
		//				if (openInEditor) {
		//					openResource((IFile) resource);
		//				}
		//			}
		return true;
	}
}
