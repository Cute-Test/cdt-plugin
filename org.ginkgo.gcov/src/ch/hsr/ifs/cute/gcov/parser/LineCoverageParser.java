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
import org.ginkgo.gcov.GcovPlugin;

public abstract class LineCoverageParser {

	protected abstract void parse(IFile cppFile, Reader gcovFile) throws CoreException, IOException;


	public void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(GcovPlugin.COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(GcovPlugin.UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(GcovPlugin.PARTIALLY_MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void runGcov(IFile file, IPath workingDirectory) {
		String[] cmdLine = {"gcov","-f","-b",file.getName()};
		File workingDir = null;
		if(workingDirectory != null){
			workingDir = workingDirectory.toFile();
		}
		String[] envp = null;
	
		Process p = null;
		try {
			p = DebugPlugin.exec(cmdLine, workingDir, envp);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		IProcess process = null;
		
		String programName = cmdLine[0];
		Map processAttributes = new HashMap();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);
		
		if (p != null) {
			process = DebugPlugin.newProcess(new Launch(null,ILaunchManager.RUN_MODE,null), p, programName, processAttributes);
			if (process == null) {
				p.destroy();
			}
			
		}
		while (!process.isTerminated()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		try {
			file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	
		
	}

	public void parse(IFile cppFile) {
		IFile gcovFile = null;
	
		IProject project = cppFile.getProject();
		String gcnoFileName = cppFile.getName().replace(cppFile.getFileExtension(),"gcno");
		IFile gcnoFile= null;
		try {
	
			gcnoFile = findFile(project, gcnoFileName);
			if(gcnoFile == null) {
				return;
			}
			
			runGcov(cppFile, gcnoFile.getParent().getLocation());
	
			String gcovFileName = cppFile.getName().concat(".gcov");
			gcovFile = findFile(project, gcovFileName);
			if (gcovFileName == null) {
				return;
			}
			
			
			deleteMarkers(cppFile);
			GcovPlugin.getDefault().getcModel().clearModel();
			parse(cppFile, new InputStreamReader(gcovFile
					.getContents()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
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