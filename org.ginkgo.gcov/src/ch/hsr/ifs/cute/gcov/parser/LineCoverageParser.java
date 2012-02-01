/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.parser.resources.GcovFile;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * 
 */
public abstract class LineCoverageParser {

	public abstract void parse(IFile cppFile, Reader gcovFile) throws CoreException, IOException;

	public void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(GcovPlugin.COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(GcovPlugin.UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(GcovPlugin.PARTIALLY_MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void runGcov(IFile file, IPath workingDirectory, IProject project) throws CoreException, IOException {
		String[] cmdLine;
		if (runningCygwin(project)) {
			cmdLine = getCygwinGcovCommand(file);
		} else {
			cmdLine = getGcovCommand(file);
		}
		File workingDir = null;
		if (workingDirectory != null) {
			workingDir = workingDirectory.toFile();
		}

		String[] envp = getEnvironmentVariables(project);

		Process p = null;

		p = DebugPlugin.exec(cmdLine, workingDir, envp);

		IProcess process = null;

		String programName = cmdLine[0];
		Map processAttributes = new HashMap();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);

		if (p != null) {
			process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, programName, processAttributes);
			if (process == null) {
				p.destroy();
				GcovPlugin.log("Gcov Process is null"); //$NON-NLS-1$
			} else {
				while (!process.isTerminated()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
			}
			try {
				file.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {
				GcovPlugin.log(e);
			}
		} else {
			GcovPlugin.log("Could not create gcov process"); //$NON-NLS-1$
		}
	}

	private String[] getGcovCommand(IFile file) {
		String[] cmdLine = { "gcov", "-f", "-b", file.getName() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return cmdLine;
	}

	private boolean runningCygwin(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = info.getManagedProject().getConfigurations()[0];
		return config.getParent().getId().contains("cygwin"); //$NON-NLS-1$
	}

	private String[] getEnvironmentVariables(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		final IEnvironmentVariableProvider environmentVariableProvider = ManagedBuildManager.getEnvironmentVariableProvider();
		IEnvironmentVariable[] variables = environmentVariableProvider.getVariables(info.getDefaultConfiguration(), true);
		String[] variableStrings = new String[variables.length];
		for (int i = 0; i < variableStrings.length; ++i) {
			variableStrings[i] = variables[i].toString();
		}
		return variableStrings;
	}

	private String[] getCygwinGcovCommand(IFile file) {
		@SuppressWarnings("nls")
		String[] cmdLine = { "sh", "-c", "'gcov", "-f", "-b", file.getName() + "'" };
		return cmdLine;
	}

	public void parse(GcovFile cppFile, IProgressMonitor monitor) throws CoreException, IOException {
		IFile gcovFile = null;
		final IFile targetFile = cppFile.getFile();
		deleteMarkers(targetFile);
		IProject project = targetFile.getProject();
		project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
		try {

			IFile gcnoFile = cppFile.getGcnoFile();
			if (gcnoFile == null) {
				return;
			}

			runGcov(targetFile, gcnoFile.getParent().getLocation(), project);

			gcovFile = cppFile.getGcovFile();
			if (gcovFile == null) {
				return;
			}

			GcovPlugin.getDefault().getcModel().clearModel();
			if (gcovFile != null) {
				parse(targetFile, new InputStreamReader(gcovFile.getContents()));
			}
		} catch (NumberFormatException e) {
			GcovPlugin.log(e);
		}
	}

	@SuppressWarnings("rawtypes")
	protected void createMarker(IFile cppFile, int lineNum, String message, String type) throws CoreException {
		Map attributes = new HashMap();
		MarkerUtilities.setMessage(attributes, message);
		MarkerUtilities.setLineNumber(attributes, lineNum);
		MarkerUtilities.createMarker(cppFile, attributes, type);
	}

	public LineCoverageParser() {
		super();
	}

}