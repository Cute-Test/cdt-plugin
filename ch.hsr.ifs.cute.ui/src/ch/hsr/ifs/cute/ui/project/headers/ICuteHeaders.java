/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.headers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author egraf
 * @since 4.0
 * 
 */
public interface ICuteHeaders {

	String getVersionNumber();

	String getVersionString();

	void copyHeaderFiles(IContainer container, IProgressMonitor monitor) throws CoreException;

	void copyTestFiles(IContainer container, IProgressMonitor monitor) throws CoreException;

	void copySuiteFiles(IContainer container, IProgressMonitor monitor, String suiteName, boolean copyTestCPP) throws CoreException;

}
