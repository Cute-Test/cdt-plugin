/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Emanuel Graf IFS
 * @since 4.0
 * 
 */
public interface ICuteWizardAdditionHandler {

	public void configureProject(IProject project, IProgressMonitor pm) throws CoreException;

	public void configureLibProject(IProject project) throws CoreException;

}
