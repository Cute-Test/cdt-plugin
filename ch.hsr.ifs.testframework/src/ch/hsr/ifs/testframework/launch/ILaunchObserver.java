/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Emanuel Graf IFS
 * @since 3.0
 * 
 */
public interface ILaunchObserver {

	public void notifyBeforeLaunch(IProject project) throws CoreException;

	public void notifyAfterLaunch(IProject project) throws CoreException;

	public void notifyTermination(IProject project) throws CoreException;

}
