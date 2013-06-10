/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.parser.resources.GcovFile;
import ch.hsr.ifs.testframework.launch.ILaunchObserver;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class LaunchObserver implements ILaunchObserver {

	public static final String GCNO_EXTENSION = "gcno";
	public static final String GCOV_EXTENSION = "gcov";

	public void notifyBeforeLaunch(IProject project) throws CoreException {
		cleanGcdaFilesInRefedProjects(project);
	}

	private void cleanGcdaFilesInRefedProjects(IProject project)
			throws CoreException {
		cleanGcdaFilesinProject(project);
		for (IProject refProj : project.getReferencedProjects()) {
			cleanGcdaFilesinProject(refProj);
		}

	}

	private void cleanGcdaFilesinProject(IProject refProj) throws CoreException {
		refProj.accept(new IResourceVisitor() {

			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String fileExtension = file.getFileExtension();
					if (fileExtension != null && fileExtension.equals("gcda")) { //$NON-NLS-1$
						file.delete(true, new NullProgressMonitor());
					}
				}
				return true;
			}
		});

	}

	public void notifyAfterLaunch(IProject project, IProgressMonitor monitor) throws CoreException {
		if (project.hasNature(GcovNature.NATURE_ID)) {
			updateGcov(project, monitor);
			for (IProject refProj : project.getReferencedProjects()) {
				updateGcov(refProj, monitor);
			}
		}

	}

	private void updateGcov(IProject iProject, IProgressMonitor monitor) {
		try {
			iProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(iProject);

			IConfiguration activeConfig = buildInfo.getDefaultConfiguration();
			IPath buildLocation = activeConfig.getEditableBuilder().getBuildLocation();
			IPath projectRelativeBuildPath = buildLocation.makeRelativeTo(iProject.getLocation());
			IFolder buildDirectory = iProject.getFolder(projectRelativeBuildPath);

			if (!buildDirectory.exists()) {
				GcovPlugin.log("Build directory is not part of the project: " + buildDirectory);
				return;
			}
			
			FileFinder gcnoFinder = new FileFinder(GCNO_EXTENSION);
			buildDirectory.accept(gcnoFinder);
			List<IFile> gcnoFiles = gcnoFinder.getFiles();
			GcovRunner gcovRunner = new GcovRunner(iProject);
			for(IFile gcnoFile : gcnoFiles)
			{
				gcovRunner.runGcov(gcnoFile);
			}
			
			iProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
			FileFinder gcovFinder = new FileFinder(GCOV_EXTENSION);
			buildDirectory.accept(gcovFinder);
			List<IFile> gcovFiles = gcovFinder.getFiles();
			for(IFile gcovFile : gcovFiles) {
				gcovRunner.parse(gcovFile, buildDirectory, monitor);
			}
			 
		} catch (CoreException ce) {
			GcovPlugin.log(ce);
		}

		// ICSourceEntry[] sourceEntries =
		// desc.getActiveConfiguration().getSourceEntries();
		// List<ICSourceEntry> sourceEntriesList = new
		// ArrayList<ICSourceEntry>();
		// ICSourceEntry[] resolvedEntries =
		// CDataUtil.resolveEntries(sourceEntries,
		// desc.getActiveConfiguration());
		// for (ICSourceEntry icSourceEntry : resolvedEntries) {
		// IPath location = icSourceEntry.getLocation();
		// if (location != null) {
		//				if (location.lastSegment() != null && !location.lastSegment().equals("cute")) { //$NON-NLS-1$
		// sourceEntriesList.add(icSourceEntry);
		// }
		// }
		// }
		// try {
		// iProject.accept(new SourceFileVisitor(sourceEntriesList));
		// } catch (CoreException e) {
		// GcovPlugin.log(e);
		// }
	}

	private final class FileFinder implements IResourceVisitor {
		private final List<IFile> files = new ArrayList<IFile>();
		private final String extension;

		public FileFinder(String extension) {
			if(extension == null){
				throw new IllegalArgumentException("Arugment 'extension' must not be null.");
			}
			this.extension = extension;
		}

		public boolean visit(IResource resource) throws CoreException {
			if(resource instanceof IFile)
			{
				IFile file = (IFile)resource;
				if(extension.equals(file.getFileExtension()))
				{
					files.add(file);
				}
			}
			return true;
		}

		public List<IFile> getFiles() {
			return files;
		}
	}
	


	private class SourceFileVisitor implements IResourceVisitor {

		class ParseJob extends Job {

			private final GcovFile file;
			private final LineCoverageParser parser = new ModelBuilderLineParser();

			public ParseJob(GcovFile file) {
				super(Messages.LaunchObserver_parse + file.getFileName());
				this.file = file;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					parser.parse(file, monitor);
				} catch (CoreException e) {
					return new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e
							.getCause().getMessage());
				} catch (IOException e) {
					return new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e
							.getCause().getMessage());
				}
				return new Status(IStatus.OK, GcovPlugin.PLUGIN_ID, "OK"); //$NON-NLS-1$
			}

		}

		private final List<ICSourceEntry> sourceEntries;

		public SourceFileVisitor(List<ICSourceEntry> sourceEntriesList) {
			this.sourceEntries = sourceEntriesList;
		}

		public boolean visit(IResource resource) throws CoreException {
			for (ICSourceEntry sourceEntry : sourceEntries) {
				if (sourceEntry.getLocation()
						.isPrefixOf(resource.getLocation())
						&& isNotInExclusion(sourceEntry, resource)) {
					if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						final GcovFile gcovFile = GcovFile.create(file);
						if (gcovFile != null) {
							parse(gcovFile);
						}
					}
				}
			}
			return true;
		}

		protected void parse(final GcovFile file) throws CoreException {
			ParseJob job = new ParseJob(file);
			job.schedule();

		}

		private boolean isNotInExclusion(ICSourceEntry sourceEntry,
				IResource resource) {
			for (IPath exPath : sourceEntry.getExclusionPatterns()) {
				if (sourceEntry.getLocation().append(exPath)
						.isPrefixOf(resource.getLocation())) {
					return false;
				}
			}
			return true;
		}
	}

}
