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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ch.hsr.ifs.cute.gcov.GcovPlugin;

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
		if(runningCygwin(project)){
			cmdLine = getCygwinGcovCommand(file);
		}else{
			cmdLine = getGcovCommand(file);
		}
		File workingDir = null;
		if(workingDirectory != null){
			workingDir = workingDirectory.toFile();
		}
		String[] envp = null;
	
		Process p = null;
		
		p = DebugPlugin.exec(cmdLine, workingDir, envp);
	
		IProcess process = null;
		
		String programName = cmdLine[0];
		Map processAttributes = new HashMap();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);
		
		if (p != null) {
			process = DebugPlugin.newProcess(new Launch(null,ILaunchManager.RUN_MODE,null), p, programName, processAttributes);
			if (process == null) {
				p.destroy();
			}else{
				while (!process.isTerminated()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
				try {
					file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					GcovPlugin.log(e);
				}
			}
		}else {
			GcovPlugin.log("Could not create gcov process"); //$NON-NLS-1$
		}
	}

	private String[] getGcovCommand(IFile file) {
		String[] cmdLine = {"gcov","-f","-b",file.getName()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return cmdLine;
	}

	private boolean runningCygwin(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = info.getManagedProject().getConfigurations()[0];
		return config.getParent().getId().contains("cygwin"); //$NON-NLS-1$
	}

	private String[] getCygwinGcovCommand(IFile file) {
		@SuppressWarnings("nls")
		String[] cmdLine = {"sh","-c","'gcov","-f","-b",file.getName()+"'"};
		return cmdLine;
	}

	public void parse(IFile cppFile) throws CoreException, IOException {
		IFile gcovFile = null;
		deleteMarkers(cppFile);
		IProject project = cppFile.getProject();
		String gcnoFileName = cppFile.getName().replace(cppFile.getFileExtension(),"gcno"); //$NON-NLS-1$
		IFile gcnoFile= null;
		try {
	
			gcnoFile = findFile(project, gcnoFileName);
			if(gcnoFile == null) {
				return;
			}
			
			runGcov(cppFile, gcnoFile.getParent().getLocation(), project);
	
			String gcovFileName = cppFile.getName().concat(".gcov"); //$NON-NLS-1$
			gcovFile = findFile(project, gcovFileName);
			if (gcovFileName == null) {
				return;
			}
			
			GcovPlugin.getDefault().getcModel().clearModel();
			if(gcovFile != null) {
				parse(cppFile, new InputStreamReader(gcovFile
						.getContents()));
			}
		} catch (NumberFormatException e) {
			GcovPlugin.log(e);
		}
	}

	protected IFile findFile(IProject project, String fileName) throws CoreException {
		FileFinderVisitor visitor = new FileFinderVisitor(fileName);
		project.accept(visitor);
		return visitor.getFile();
	
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