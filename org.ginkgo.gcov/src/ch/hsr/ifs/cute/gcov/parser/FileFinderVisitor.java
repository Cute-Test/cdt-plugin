/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

class FileFinderVisitor implements IResourceVisitor {
	private String sourceFileName;
	private IFile file;

	FileFinderVisitor(String FileName) {
		sourceFileName = FileName;
	}
	
	

	public IFile getFile() {
		return file;
	}

	public boolean visit(IResource resource) throws CoreException {
		if (resource.getName().equals(sourceFileName)) {
			file = (IFile) resource;
			return false;
		} else {
			return true;
		}
	}

}