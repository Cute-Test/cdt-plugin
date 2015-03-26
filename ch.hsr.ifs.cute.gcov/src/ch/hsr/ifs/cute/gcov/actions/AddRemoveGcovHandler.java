/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.actions;

import static ch.hsr.ifs.cute.gcov.util.ProjectUtil.getConfiguration;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.ui.GcovAdditionHandler;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * 
 */
public class AddRemoveGcovHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IProject project = getSelectedProject(selection);
		String action = event.getParameter("ch.hsr.ifs.cute.gcov.handleGcovNatrueParameter");
		switch (action) {
		case "add":
			addNatureToProject(project);
			break;
		case "remove":
			removeNatureFromProject(project);
			break;
		}
		return null;
	}

	private void addNatureToProject(IProject project) {
		if (project != null) {
			try {
				GcovNature.addGcovNature(project, new NullProgressMonitor());
				new GcovAdditionHandler().addGcovConfig(project);
				notifyUserSuccedd("Gcov Coverage Analysis successfull added to project.");
			} catch (CoreException e) {
				GcovPlugin.log(e);
			}
		} else {
			notifyUserInvalidSelection();
		}
	}

	private void notifyUserInvalidSelection() {
		IWorkbenchWindow activeWindow = getActiveWorkbenchWindow();
		if (activeWindow != null) {
			MessageDialog.openError(activeWindow.getShell(), "Invalid Selection",
					"Adding/removing Gcov Coverage failed due to invalid selection. Please select a C/C++ project.");
		}
	}

	private IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	private void removeNatureFromProject(IProject project) {
		if (project != null) {
			try {
				GcovNature.removeCuteNature(project, new NullProgressMonitor());
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
				IConfiguration[] configs = info.getManagedProject().getConfigurations();
				if (getConfiguration(project).getId().equals(GcovAdditionHandler.GCOV_CONFG_ID)) {
					for (IConfiguration config : configs) {
						if (config.getParent().getId().contains("debug") && !config.getName().contains("Gcov")) {
							ManagedBuildManager.setSelectedConfiguration(project, config);
							ManagedBuildManager.setDefaultConfiguration(project, config);
						}
					}
				}
				info.getManagedProject().removeConfiguration(GcovAdditionHandler.GCOV_CONFG_ID);
				ManagedBuildManager.updateCoreSettings(project);
				notifyUserSuccedd("Gcov Coverage Analysis successfull removed from project.");
			} catch (CoreException e) {
				GcovPlugin.log(e);
			}
		} else {
			notifyUserInvalidSelection();
		}
	}

	private void notifyUserSuccedd(String message) {
		IWorkbenchWindow activeWindow = getActiveWorkbenchWindow();
		if (activeWindow != null) {
			MessageDialog.openInformation(activeWindow.getShell(), "Success", message);
		}
	}

	private IProject getSelectedProject(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return null;

		for (Object selected : ((IStructuredSelection) selection).toList()) {
			if (selected instanceof IProject) {
				return (IProject) selected;
			} else if (selected instanceof IAdaptable) {
				IProject proj = (IProject) ((IAdaptable) selected).getAdapter(IProject.class);
				if (proj != null) {
					return proj;
				}
			} else if (selected instanceof ICElement) {
				return ((ICElement) selected).getCProject().getProject();
			}
		}
		return null;
	}
}
