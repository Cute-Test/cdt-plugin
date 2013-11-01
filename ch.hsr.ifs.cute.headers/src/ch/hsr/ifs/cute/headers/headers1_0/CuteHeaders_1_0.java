/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.headers1_0;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.cute.headers.utils.CopyUtils;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author egraf
 * 
 */
public class CuteHeaders_1_0 implements ICuteHeaders {

	public CuteHeaders_1_0() {
	}

	public String getVersionNumber() {
		return "1.0";
	}

	public String getVersionString() {
		return "CUTE Headers 1.0.0";
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
