/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.headers2_1;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.cute.headers.utils.CopyUtils;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 * @author tcorbat
 * @author psommerl
 * @since 2.1
 * 
 */
public class CuteHeaders_2_1 implements ICuteHeaders {

	public String getVersionNumber() {
		return "2.1";
	}

	public String getVersionString() {
		return "CUTE Headers 2.1.0";
	}

	public void copyHeaderFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
		CopyUtils.copyHeaderFiles(container, monitor, getVersionNumber());

	}

	public void copySuiteFiles(IContainer container, IProgressMonitor monitor, String suitename, boolean copyTestCPP) throws CoreException {
		CopyUtils.copySuiteFiles(container, monitor, suitename, copyTestCPP, getVersionNumber());
	}

	public void copyTestFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
		CopyUtils.copyTestFiles(container, monitor, getVersionNumber());
	}
}