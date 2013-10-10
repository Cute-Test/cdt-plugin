/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
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
 * @since 4.0
 * 
 */
public class ChangeCuteVersionWizard extends Wizard {

	private final IProject project;
	private ChangeCuteVersionWizardPage page;
	private String activeHeadersVersionName;

	public ChangeCuteVersionWizard(IProject project) {
		this.project = project;
		try {
			activeHeadersVersionName = project.getPersistentProperty(UiPlugin.CUTE_VERSION_PROPERTY_NAME);
		} catch (CoreException e) {
			//activeHeadersVersionName remains null
		}
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new ChangeCuteVersionWizardPage(activeHeadersVersionName);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		try {
			ICuteHeaders cuteVersion = getCuteVersion(page.getVersionString());
			if (cuteVersion.getVersionString().equals(activeHeadersVersionName)) {
				return true; //do nothing since same version was chosen as before
			}
			replaceHeaders(cuteVersion);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	private void replaceHeaders(final ICuteHeaders cuteVersion) throws InvocationTargetException, InterruptedException {
		this.getContainer().run(true, false, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IFolder cuteFolder = project.getFolder("cute");
				try {
					IResource[] files = cuteFolder.members();
					SubMonitor mon = SubMonitor.convert(monitor, files.length * 2);
					for (IResource resource : files) {
						mon.subTask(MessageFormat.format(Messages.getString("ChangeCuteVersionWizard.Remove"), resource.getName())); //$NON-NLS-1$
						resource.delete(true, new NullProgressMonitor());
						mon.worked(1);
					}
					cuteVersion.copyHeaderFiles(cuteFolder, mon.newChild(files.length));
					project.setPersistentProperty(UiPlugin.CUTE_VERSION_PROPERTY_NAME, cuteVersion.getVersionString());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected ICuteHeaders getCuteVersion(String cuteVersionString) {
		SortedSet<ICuteHeaders> headers = UiPlugin.getInstalledCuteHeaders();
		for (ICuteHeaders cuteHeaders : headers) {
			if (cuteVersionString.equals(cuteHeaders.getVersionString()))
				return cuteHeaders;
		}
		return null;
	}

}
