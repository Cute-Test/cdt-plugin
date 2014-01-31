/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

class FileFinderVisitor implements IResourceVisitor {
	private final String sourceFileName;
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