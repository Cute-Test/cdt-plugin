/*******************************************************************************
 * Copyright (c) 2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.project.CuteNature;

public class AddRemoveCuteNatureAction extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		Collection<IProject> selectedProjects = updateSelection(selection);
		String action = event.getParameter("ch.hsr.ifs.cute.ui.handleCuteNatureParameter");
		switch (action) {
		case "add":
			addCuteNatureToSelectedProjects(selectedProjects);
			break;
		case "remove":
			removeCuteNatureFromSelectedProjects(selectedProjects);
			break;
		}
		return null;
	}

	private void addCuteNatureToSelectedProjects(Collection<IProject> selectedProjects) {
		for (IProject p : selectedProjects) {
			try {
				CuteNature.addCuteNature(p, new NullProgressMonitor());
			} catch (CoreException e) {
				CuteUIPlugin.log("Adding CUTE nature failed", e);
			}
		}
	}
	
	private void removeCuteNatureFromSelectedProjects(Collection<IProject> selectedProjects) {
		for (IProject p : selectedProjects) {
			try {
				CuteNature.removeCuteNature(p, new NullProgressMonitor());
			} catch (CoreException e) {
				CuteUIPlugin.log("Removing CUTE nature failed", e);
			}
		}
	}
	
	protected Collection<IProject> updateSelection(ISelection selection) {
		Collection<IProject> selectedProjects = new ArrayList<>();
		if (!(selection instanceof IStructuredSelection))
			return selectedProjects;
	
		selectedProjects.clear();
	
		for (Object selected : ((IStructuredSelection) selection).toList()) {
			if (selected instanceof IProject) {
				selectedProjects.add((IProject) selected);
			} else if (selected instanceof IAdaptable) {
				IProject proj = (IProject) ((IAdaptable) selected).getAdapter(IProject.class);
				if (proj != null) {
					selectedProjects.add(proj);
				}
			} else if (selected instanceof ICElement) {
				selectedProjects.add(((ICElement) selected).getCProject().getProject());
			}
		}
		return selectedProjects;
	}
}
