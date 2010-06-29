/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
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

/**
 * @author Emanuel Graf IFS
 *
 */
public class RemoveGcovAction implements IWorkbenchWindowActionDelegate{
	
	private IProject project;

	/**
	 * 
	 */
	public RemoveGcovAction() {
		// TODO Auto-generated constructor stub
	}

	public void run(IAction action) {
		if(project != null) {
			try {
				GcovNature.removeCuteNature(project, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			};
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
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

}
