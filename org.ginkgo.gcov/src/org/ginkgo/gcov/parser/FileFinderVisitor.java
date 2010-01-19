package org.ginkgo.gcov.parser;

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