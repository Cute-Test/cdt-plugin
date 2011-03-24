/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Emanuel Graf IFS
 *
 */
public class GcovBuilder extends IncrementalProjectBuilder {
	
	public static final String BUILDER_ID = GcovPlugin.PLUGIN_ID + ".gcovBuilder"; //$NON-NLS-1$

	/**
	 * 
	 */
	public GcovBuilder() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		project.accept(new IResourceVisitor() {
			
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String fileExt = file.getFileExtension();
					if(fileExt != null && (fileExt.equals("gcno")|| fileExt.equals("gcda")||fileExt.equals("gcov"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						file.delete(true, new NullProgressMonitor());
					}
				}
				return true;
			}
		});
	}
	

}
