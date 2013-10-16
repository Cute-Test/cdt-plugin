/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

public class SuiteTemplateCopyUtil {

	public static void copyFile(IContainer container, IProgressMonitor monitor, String templateFilename, String targetFilename, String suitename) throws CoreException {
		IFile targetFile = container.getFile(new Path(targetFilename));
		copyFile(targetFile, monitor, templateFilename, suitename);
	}

	@SuppressWarnings({ "rawtypes" })
	public static void copyFile(IFile targetFile, IProgressMonitor monitor, String templateFilename, String suitename) throws CoreException {
		Enumeration en = CuteHeaders15Plugin.getDefault().getBundle().findEntries("suite", templateFilename, false);
		if (en.hasMoreElements()) {
			URL url = (URL) en.nextElement();

			try {
				ByteArrayInputStream str = implantActualsuitename(url, suitename);

				targetFile.create(str, IResource.FORCE, new SubProgressMonitor(monitor, 1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CuteHeaders15Plugin.PLUGIN_ID, 42, e.getMessage(), e));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, CuteHeaders15Plugin.PLUGIN_ID, 42, "missing suite template files", null));
		}
	}

	public static ByteArrayInputStream implantActualsuitename(URL url, String suitename) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder buffer = new StringBuilder();
		String linesep = System.getProperty("line.separator");
		while (br.ready()) {
			String a = br.readLine();
			buffer.append(a.replaceAll("[$]suitename[$]", suitename) + linesep);
		}
		br.close();
		return new ByteArrayInputStream(buffer.toString().getBytes());
	}
}
