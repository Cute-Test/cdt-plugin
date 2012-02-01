/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.actions;

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
 * 
 */
public class AddGcovAction implements IWorkbenchWindowActionDelegate {

	private IProject project;

	/**
	 * 
	 */
	public AddGcovAction() {
	}

	public void run(IAction action) {
		if (project != null) {
			try {
				GcovNature.addGcovNature(project, new NullProgressMonitor());
				new GcovAdditionHandler().addGcovConfig(project);
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
