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
package ch.hsr.ifs.cute.headers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 *
 */
public class CuteHeades_1_6 implements ICuteHeaders {

	/**
	 * 
	 */
	public CuteHeades_1_6() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("nls")
	private List<URL> getHeaderFiles() {
		return getFileListe("headers", "*.*");
	}

	@SuppressWarnings("unchecked")
	private List<URL> getFileListe(String path, String filePattern) {
		Enumeration en = Activator.getDefault().getBundle().findEntries(path, filePattern, false);
		List<URL>list = new ArrayList<URL>();
		while (en.hasMoreElements()) {
			list.add((URL) en.nextElement());
		}
		return list;
	}

	public double getVersionNumber() {
		return 1.60;
	}

	public String getVersionString() {
		return "Cute Headers 1.6.0"; //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	private List<URL> getTestFiles() {
		return getFileListe("test", "*.*");
	}

	public void copyHeaderFiles(IFolder folder, IProgressMonitor monitor)
			throws CoreException {
		copyFilesToFolder(folder, monitor, getHeaderFiles());
		
	}

	private void copyFilesToFolder(IFolder folder, IProgressMonitor monitor,
			List<URL> urls) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, urls.size());
		for (URL url : urls) {
			String[] elements = url.getFile().split("/"); //$NON-NLS-1$
			String filename = elements[elements.length-1];
			mon.subTask("Copy " + filename);
			IFile targetFile = folder.getFile(filename);
			try {
				targetFile.create(url.openStream(),IResource.FORCE , new SubProgressMonitor(monitor,1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,42,e.getMessage(), e));
			}
			mon.worked(1);
			mon.done();
		}
	}

	public void copySuiteFiles(IFolder folder, IProgressMonitor monitor,
			String suitename) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, 3);
		mon.subTask("Copy Test.cpp");
		SuiteTemplateCopyUtil.copyFile(folder,monitor,"Test.cpp","Test.cpp",suitename); //$NON-NLS-1$ //$NON-NLS-2$
		mon.worked(1);
		mon.subTask("Copy Suite");
		SuiteTemplateCopyUtil.copyFile(folder,monitor,"$suitename$.cpp",suitename+".cpp",suitename); //$NON-NLS-1$ //$NON-NLS-2$
		mon.worked(1);
		SuiteTemplateCopyUtil.copyFile(folder,monitor,"$suitename$.h",suitename+".h",suitename); //$NON-NLS-1$ //$NON-NLS-2$
		mon.worked(1);
		mon.done();
	}

	public void copyTestFiles(IFolder folder, IProgressMonitor monitor)
			throws CoreException {
		copyFilesToFolder(folder, monitor, getTestFiles());
		
	}

}