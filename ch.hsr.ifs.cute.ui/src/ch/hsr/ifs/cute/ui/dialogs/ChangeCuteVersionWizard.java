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

import ch.hsr.ifs.cute.core.headers.CuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;

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
			activeHeadersVersionName = project.getPersistentProperty(CuteUIPlugin.CUTE_VERSION_PROPERTY_NAME);
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
			CuteHeaders cuteVersion = getCuteVersion(page.getVersionString());
			if (cuteVersion.getVersionString().equals(activeHeadersVersionName)) {
				return true; //do nothing since same version was chosen as before
			}
			replaceHeaders(cuteVersion);
		} catch (InvocationTargetException e) {
			CuteUIPlugin.log("Exception while performing version change", e);
		} catch (InterruptedException e) {
			CuteUIPlugin.log("Exception while performing version change", e);
		}
		return true;
	}

	private void replaceHeaders(final CuteHeaders cuteVersion) throws InvocationTargetException, InterruptedException {
		this.getContainer().run(true, false, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) {
				IFolder cuteFolder = project.getFolder("cute");
				try {
					if (!cuteFolder.exists()) {
						cuteFolder.create(true, true, monitor);
					}
					IResource[] files = cuteFolder.members();
					SubMonitor mon = SubMonitor.convert(monitor, files.length * 2);
					for (IResource resource : files) {
						mon.subTask(MessageFormat.format(Messages.getString("ChangeCuteVersionWizard.Remove"), resource.getName()));
						resource.delete(true, new NullProgressMonitor());
						mon.worked(1);
					}
					cuteVersion.copyHeaderFiles(cuteFolder, mon.newChild(files.length));
					project.setPersistentProperty(CuteUIPlugin.CUTE_VERSION_PROPERTY_NAME, cuteVersion.getVersionString());
				} catch (CoreException e) {
					CuteUIPlugin.log("Exception while replacing headers", e);
				}
			}

		});
	}

	protected CuteHeaders getCuteVersion(String cuteVersionString) {
		SortedSet<CuteHeaders> headers = CuteUIPlugin.getInstalledCuteHeaders();
		for (CuteHeaders cuteHeaders : headers) {
			if (cuteVersionString.equals(cuteHeaders.getVersionString()))
				return cuteHeaders;
		}
		return null;
	}

}
