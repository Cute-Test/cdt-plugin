/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 * @since 2.0
 * 
 */
public class CuteHeaders_1_6 implements ICuteHeaders {

	public CuteHeaders_1_6() {
	}

	private List<URL> getHeaderFiles() {
		return getFileListe("headers", "*.*");
	}

	@SuppressWarnings({ "rawtypes" })
	private List<URL> getFileListe(String path, String filePattern) {
		Enumeration en = CuteHeaders16Plugin.getDefault().getBundle().findEntries(path, filePattern, false);
		List<URL> list = new ArrayList<URL>();
		while (en.hasMoreElements()) {
			list.add((URL) en.nextElement());
		}
		return list;
	}

	public double getVersionNumber() {
		return 1.63;
	}

	public String getVersionString() {
		return "CUTE Headers 1.6.3";
	}

	private List<URL> getTestFiles() {
		return getFileListe("test", "*.*");
	}

	public void copyHeaderFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
		copyFilesToFolder(container, monitor, getHeaderFiles());

	}

	private void copyFilesToFolder(IContainer container, IProgressMonitor monitor, List<URL> urls) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, urls.size());
		for (URL url : urls) {
			String[] elements = url.getFile().split("/");
			String filename = elements[elements.length - 1];
			mon.subTask(Messages.CuteHeades_1_6_copy + filename);
			IFile targetFile = container.getFile(new Path(filename));
			try {
				targetFile.create(url.openStream(), IResource.FORCE, new SubProgressMonitor(monitor, 1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CuteHeaders16Plugin.PLUGIN_ID, 42, e.getMessage(), e));
			}
			mon.worked(1);
			mon.done();
		}
	}

	public void copySuiteFiles(IContainer container, IProgressMonitor monitor, String suitename, boolean copyTestCPP) throws CoreException {
		SubMonitor mon;
		if (copyTestCPP) {
			mon = SubMonitor.convert(monitor, 3);
			mon.subTask(Messages.CuteHeades_1_6_copyTestCPP);
			SuiteTemplateCopyUtil.copyFile(container, monitor, "Test.cpp", "Test.cpp", suitename);
			mon.worked(1);
		} else {
			mon = SubMonitor.convert(monitor, 2);
		}
		mon.subTask(Messages.CuteHeades_1_6_copySuite);
		SuiteTemplateCopyUtil.copyFile(container, monitor, "$suitename$.cpp", suitename + ".cpp", suitename);
		mon.worked(1);
		SuiteTemplateCopyUtil.copyFile(container, monitor, "$suitename$.h", suitename + ".h", suitename);
		mon.worked(1);
		mon.done();
	}

	public void copyTestFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
		copyFilesToFolder(container, monitor, getTestFiles());
	}
}
