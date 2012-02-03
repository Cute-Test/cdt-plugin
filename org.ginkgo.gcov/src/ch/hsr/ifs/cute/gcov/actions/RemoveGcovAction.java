/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.actions;

import static ch.hsr.ifs.cute.gcov.util.ProjectUtil.getConfiguration;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.ui.GcovAdditionHandler;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class RemoveGcovAction implements IWorkbenchWindowActionDelegate {

	private IProject project;

	public RemoveGcovAction() {
	}

	public void run(IAction action) {
		if (project != null) {
			try {
				GcovNature.removeCuteNature(project, new NullProgressMonitor());
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
				IConfiguration[] configs = info.getManagedProject().getConfigurations();
				if (getConfiguration(project).getId().equals(GcovAdditionHandler.GCOV_CONFG_ID)) {
					for (IConfiguration config : configs) {
						if (config.getParent().getId().contains("debug") && !config.getName().contains("Gcov")) { //$NON-NLS-1$ //$NON-NLS-2$
							ManagedBuildManager.setSelectedConfiguration(project, config);
							ManagedBuildManager.setDefaultConfiguration(project, config);
						}
					}
				}
				info.getManagedProject().removeConfiguration(GcovAdditionHandler.GCOV_CONFG_ID);
				ManagedBuildManager.updateCoreSettings(project);

			} catch (CoreException e) {
				GcovPlugin.log(e);
			}
			project = null;
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TreeSelection) {
			TreeSelection treeSel = (TreeSelection) selection;
			if (treeSel.getFirstElement() instanceof IProject) {
				project = (IProject) treeSel.getFirstElement();
			}
		}
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

}
