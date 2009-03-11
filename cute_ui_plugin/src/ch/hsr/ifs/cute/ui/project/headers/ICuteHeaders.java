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
package ch.hsr.ifs.cute.ui.project.headers;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author egraf
 *
 */
public interface ICuteHeaders {
	
	double getVersionNumber();
	String getVersionString();
	void copyHeaderFiles(IFolder folder, IProgressMonitor monitor) throws CoreException;
	void copyTestFiles(IFolder folder, IProgressMonitor monitor) throws CoreException;
	void copySuiteFiles(IFolder folder, IProgressMonitor monitor, String suiteName) throws CoreException;

}
