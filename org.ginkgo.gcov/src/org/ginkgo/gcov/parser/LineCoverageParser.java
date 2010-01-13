package org.ginkgo.gcov.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class LineCoverageParser implements IParser {

	public static final String UNCOVER_MARKER_TYPE = "org.ginkgo.gcov.lineUnCoverMarker";
	public static final String COVER_MARKER_TYPE = "org.ginkgo.gcov.lineCoverMarker";
	public static final String COVERAGE_MARKER_TYPE = "org.ginkgo.gcov.CoverageMarker";

	private IFile gcovFile = null;

	private class MyResourceVisitor implements IResourceVisitor {
		private String sourceFileName;
		private IFile file;

		private MyResourceVisitor(String FileName) {
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void runGcov(IFile file, IPath workingDirectory) {
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

	@SuppressWarnings("rawtypes")
	public void parse(IFile cppFile) {
		String lineNum = null;
		String execCount = null;
		String line = null;
		gcovFile = null;

		IProject project = cppFile.getProject();
		String gcnoFileName = cppFile.getName().replace(cppFile.getFileExtension(),"gcno");
		IFile gcnoFile= null;
		try {
			MyResourceVisitor visitor = new MyResourceVisitor(gcnoFileName);
			project.accept(visitor);
			gcnoFile = visitor.getFile();
			if(gcnoFile == null) {
				return;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		runGcov(cppFile, gcnoFile.getParent().getLocation());
		
		String gcovFileName = cppFile.getName().concat(".gcov");
		try {
			MyResourceVisitor visitor = new MyResourceVisitor(gcovFileName);
			project.accept(visitor);
			gcovFile = visitor.getFile();
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if (gcovFileName == null) {
			return;
		}
		deleteMarkers(cppFile);
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(gcovFile
					.getContents()));
			while ((line = in.readLine()) != null) {
				String[] s = line.split(":");
				if (s.length == 3) {
					execCount = s[0].trim();
					lineNum = s[1].trim();
					if (execCount.equals("#####")) {
						Map attributes = new HashMap();
						MarkerUtilities.setMessage(attributes, "Not Covered");
						MarkerUtilities.setLineNumber(attributes, Integer
								.parseInt(lineNum));
						MarkerUtilities.createMarker(cppFile, attributes,
								UNCOVER_MARKER_TYPE);
					} else if (execCount.equals("-")) {

					} else {
						Map attributes = new HashMap();
						MarkerUtilities.setMessage(attributes, execCount);
						MarkerUtilities.setLineNumber(attributes, Integer
								.parseInt(lineNum));
						MarkerUtilities.createMarker(cppFile, attributes,
								COVER_MARKER_TYPE);
					}
					continue;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

}
