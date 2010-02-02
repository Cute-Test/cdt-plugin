/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.SortedSet;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 *
 */
public class ChangeCuteVersionWizard extends Wizard {
	
	private IProject project;
	private ChangeCuteVersionWizardPage page;

	public ChangeCuteVersionWizard(IProject project) {
		this.project = project;
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new ChangeCuteVersionWizardPage();
		addPage(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			this.getContainer().run(true, false, new IRunnableWithProgress() {
			
			private ICuteHeaders cuteVersion = getCuteVersion(page.getVersionString());

			public void run(IProgressMonitor monitor) {
			if (project != null) {
				IFolder cuteFolder = project.getFolder("cute"); //$NON-NLS-1$
				try {
					IResource[] files = cuteFolder.members();
					SubMonitor mon = SubMonitor.convert(monitor, files.length * 2);
					for (IResource resource : files) {
						mon.subTask(MessageFormat.format(Messages.getString("ChangeCuteVersionWizard.Remove"), resource.getName())); //$NON-NLS-1$
						resource.delete(true, new NullProgressMonitor());
						mon.worked(1);
					}
					cuteVersion.copyHeaderFiles(cuteFolder, mon.newChild(files.length));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	protected ICuteHeaders getCuteVersion(String cuteVersionString) {
		SortedSet<ICuteHeaders> headers = UiPlugin.getInstalledCuteHeaders();
		for (ICuteHeaders cuteHeaders : headers) {
			if(cuteVersionString.equals(cuteHeaders.getVersionString()))
				return cuteHeaders;
		}
		return null;
	}

}
