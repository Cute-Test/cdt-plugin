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
