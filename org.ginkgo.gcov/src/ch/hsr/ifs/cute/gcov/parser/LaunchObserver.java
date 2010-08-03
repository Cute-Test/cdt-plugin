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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.hsr.ifs.cute.core.launch.ILaunchObserver;
import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class LaunchObserver implements ILaunchObserver {

	public void notifyBeforeLaunch(IProject project) throws CoreException {
		cleanGcdaFilesInRefedProjects(project);
	}
	
	private void cleanGcdaFilesInRefedProjects(IProject project) throws CoreException {
		for(IProject refProj : project.getReferencedProjects()) {
			cleanGcdaFilesinProject(refProj);
		}
		
	}

	private void cleanGcdaFilesinProject(IProject refProj) throws CoreException {
		refProj.accept(new IResourceVisitor() {
			
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String fileExtension = file.getFileExtension();
					if(fileExtension != null && fileExtension.equals("gcda")) { //$NON-NLS-1$
						file.delete(true, new NullProgressMonitor());
					}
				}
				return true;
			}
		});
		
	}


	public void notifyAfterLaunch(IProject project) throws CoreException {
		if (project.hasNature(GcovNature.NATURE_ID)) {
			updateGcov(project);
			for (IProject refProj : project.getReferencedProjects()) {
				updateGcov(refProj);
			}
		}

	}

	private void updateGcov(IProject iProject) {
		ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(iProject);
		ICSourceEntry[] sourceEntries = desc.getActiveConfiguration().getSourceEntries();
		List<ICSourceEntry> sourceEntriesList = new ArrayList<ICSourceEntry>();
		for (ICSourceEntry icSourceEntry : sourceEntries) {
			IPath location = icSourceEntry.getLocation();
			if (location != null) {
				if (location.lastSegment() != null && !location.lastSegment().equals("cute")) { //$NON-NLS-1$
					sourceEntriesList.add(icSourceEntry);
				}
			}
		}
		try {
			iProject.accept(new SourceFileVisitor(sourceEntriesList));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private class SourceFileVisitor implements IResourceVisitor {

		class ParseJob extends Job {

			private IFile file;
			private LineCoverageParser parser = new ModelBuilderLineParser();

			public ParseJob(IFile file) {
				super(Messages.LaunchObserver_parse + file.getName());
				this.file = file;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				parser.parse(file);
				return new Status(IStatus.OK, GcovPlugin.PLUGIN_ID, "OK"); //$NON-NLS-1$
			}

		}

		private List<ICSourceEntry> sourceEntries;

		public SourceFileVisitor(List<ICSourceEntry> sourceEntriesList) {
			this.sourceEntries = sourceEntriesList;
		}

		public boolean visit(IResource resource) throws CoreException {
			for (ICSourceEntry sourceEntry : sourceEntries) {
				if (sourceEntry.getLocation().isPrefixOf(resource.getLocation())
						&& isNotInExclusion(sourceEntry, resource)) {
					if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						if (file.getName().endsWith("cpp")) { //$NON-NLS-1$
							parse(file);
						}
					}
				}
			}
			return true;
		}

		protected void parse(final IFile file) throws CoreException {
			ParseJob job = new ParseJob(file);
			job.schedule();

		}

		private boolean isNotInExclusion(ICSourceEntry sourceEntry, IResource resource) {
			for (IPath exPath : sourceEntry.getExclusionPatterns()) {
				if (sourceEntry.getLocation().append(exPath).isPrefixOf(resource.getLocation())) {
					return false;
				}
			}
			return true;
		}
	}

}
