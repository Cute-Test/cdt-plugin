package ch.hsr.ifs.cute.gcov.parser;

import static ch.hsr.ifs.cute.gcov.util.ProjectUtil.getConfiguration;
import static ch.hsr.ifs.cute.gcov.util.StreamUtil.tryClose;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import ch.hsr.ifs.cute.gcov.model.Branch;
import ch.hsr.ifs.cute.gcov.model.CoverageStatus;
import ch.hsr.ifs.cute.gcov.model.Function;
import ch.hsr.ifs.cute.gcov.model.Line;
import ch.hsr.ifs.cute.gcov.parser.resources.GcovFile;

public class GcovRunner {

	private IProject project;

	public GcovRunner(IProject iProject) {
		this.project = iProject;
	}
	
	private boolean runningCygwin() {
		final IConfiguration config = getConfiguration(project);
		if (config != null) {
			final IToolChain toolChain = config.getToolChain();
			if (toolChain != null) {
				return toolChain.getName().startsWith("Cygwin"); //$NON-NLS-1$
			}
		}
		return false;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void runGcov(IFile gcovFile) throws CoreException {

		String fileName = gcovFile.getFullPath().lastSegment();
		String[] cmdLine;
		if (runningCygwin()) {
			cmdLine = getCygwinGcovCommand(fileName);
		} else {
			cmdLine = getGcovCommand(fileName);
		}

		String[] envp = getEnvironmentVariables();

		Process p = null;

		p = DebugPlugin.exec(cmdLine, new File(gcovFile.getLocation().removeLastSegments(1).toOSString()), envp);

		IProcess process = null;

		String programName = cmdLine[0];
		Map processAttributes = new HashMap();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);

		if (p != null) {
			final Launch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
			process = DebugPlugin.newProcess(launch, p, programName, processAttributes);
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
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {
				GcovPlugin.log(e);
			}
		} else {
			GcovPlugin.log("Could not create gcov process"); //$NON-NLS-1$
		}
	}
	
	private String[] getCygwinGcovCommand(String iPath) {
		@SuppressWarnings("nls")
		String[] cmdLine = { "sh", "-c", "'gcov", "-f", "-b", iPath + "'" };
		return cmdLine;
	}
	
	private String[] getGcovCommand(String iPath) {
		String[] cmdLine = { "gcov", "-f", "-b", iPath }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return cmdLine;
	}
	
	private String[] getEnvironmentVariables() {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		final IEnvironmentVariableProvider environmentVariableProvider = ManagedBuildManager.getEnvironmentVariableProvider();
		IEnvironmentVariable[] variables = environmentVariableProvider.getVariables(info.getDefaultConfiguration(), true);
		String[] variableStrings = new String[variables.length];
		for (int i = 0; i < variableStrings.length; ++i) {
			variableStrings[i] = variables[i].toString();
		}
		return variableStrings;
	}
	
	public void parse(IFile gcovFile, IFolder buildDirectory, IProgressMonitor monitor) throws CoreException {
//		deleteMarkers(targetFile);
		IProject project = gcovFile.getProject();

		try {
			GcovPlugin.getDefault().getcModel().clearModel();
			if (gcovFile != null) {
				final InputStreamReader gcovFileInput = new InputStreamReader(gcovFile.getContents());
				try {
					parse(gcovFileInput, buildDirectory, project);
				} finally {
					tryClose(gcovFileInput);
					gcovFile.delete(true, monitor);
				}
			}
		} catch (NumberFormatException e) {
			GcovPlugin.log(e);
		}
	}
	
	public void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(GcovPlugin.COVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(GcovPlugin.UNCOVER_MARKER_TYPE, true, IResource.DEPTH_ZERO);
			file.deleteMarkers(GcovPlugin.PARTIALLY_MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}
	
	private static final Pattern LINE_PATTERN = Pattern.compile("(.*)(\\d+|-|#####):(\\s*)(\\d+):(.*)$"); //$NON-NLS-1$
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("(function )(\\w*)( called )(\\d+)( returned \\d+\\% blocks executed )(\\d+)(\\%)(.*)$"); //$NON-NLS-1$
	private static final Pattern BRANCH_PATTERN = Pattern.compile("(branch\\s+)(\\d+)(\\s+taken\\s+)(\\d+)(\\%)(.*)$"); //$NON-NLS-1$
	private static final Pattern FILE_PATTERN = Pattern.compile("(.*)(0:Source:)(.*)$");

	Function currentFunction;
	Line currentLine;

	public void parse(Reader gcovFile, IFolder baseFolder, IProject project) throws CoreException {
		IFile cppFile = null;
		ch.hsr.ifs.cute.gcov.model.File file = null; //GcovPlugin.getDefault().getcModel().addFileToModel(cppFile);
		BufferedReader in = new BufferedReader(gcovFile);
		String line;
		try {
			try {
				while ((line = in.readLine()) != null) {
					Matcher sourceFileMatcher = FILE_PATTERN.matcher(line);
					if(sourceFileMatcher.matches())
					{
						String sourceFileName = sourceFileMatcher.group(3);
						
						IPath sourceFilePath = baseFolder.getFullPath().append(sourceFileName);
						IFile sourceFile = project.getWorkspace().getRoot().getFile(sourceFilePath);
						if(!sourceFile.exists()){
							return;
						}
						deleteMarkers(sourceFile);
						cppFile = sourceFile;
						file = GcovPlugin.getDefault().getcModel().addFileToModel(sourceFile);
						continue;
					}
					Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
					if (functionMatcher.matches()) {
						handleFunction(functionMatcher, file);
						continue;
					}
					Matcher lineMatcher = LINE_PATTERN.matcher(line);
					if (lineMatcher.matches()) {
						handleLine(lineMatcher);
						continue;
					}
					Matcher branchMatcher = BRANCH_PATTERN.matcher(line);
					if (branchMatcher.matches()) {
						int taken = Integer.parseInt(branchMatcher.group(4));
						currentLine.addBranch(new Branch(taken));
					}

				}
			} catch (NumberFormatException e) {
				GcovPlugin.log(e);
			} catch (IOException e) {
				GcovPlugin.log(e);
			}
		} finally {
			tryClose(in);
		}
		if(file != null) {
			for (Function f : file.getFunctions()) {
				for (Line l : f.getLines()) {
					switch (l.getStatus()) {
					case Covered:
						createMarker(cppFile, l.getNr(), "covered", GcovPlugin.COVER_MARKER_TYPE); //$NON-NLS-1$
						break;
					case PartiallyCovered:
						createMarker(cppFile, l.getNr(), "partially covered", GcovPlugin.PARTIALLY_MARKER_TYPE); //$NON-NLS-1$
						break;
					case Uncovered:
						createMarker(cppFile, l.getNr(), "uncovered", GcovPlugin.UNCOVER_MARKER_TYPE); //$NON-NLS-1$
						break;
					default:
						break;
					}
				}
			}
		}
	}

	protected void handleFunction(Matcher functionMatcher, ch.hsr.ifs.cute.gcov.model.File file) {
		try {
			String name = functionMatcher.group(2);
			int called = Integer.parseInt(functionMatcher.group(4));
			int execBlocks = Integer.parseInt(functionMatcher.group(6));
			currentFunction = new Function(name, called, execBlocks);
			file.addFunction(currentFunction);
		} catch (NumberFormatException e) {
		}
	}

	protected void handleLine(Matcher lineMatcher) {
		try {
			String count = lineMatcher.group(2);
			int lineNumber = Integer.parseInt(lineMatcher.group(4));
			if (count.equalsIgnoreCase("#####")) { //$NON-NLS-1$
				currentLine = new Line(lineNumber, CoverageStatus.Uncovered);
				currentFunction.addLine(currentLine);
			} else if (count.equalsIgnoreCase("-")) { //$NON-NLS-1$

			} else {
				int i = Integer.parseInt(count);
				if (i > 0) {
					currentLine = new Line(lineNumber, CoverageStatus.Covered);
					currentFunction.addLine(currentLine);
				} else {
					currentLine = new Line(lineNumber, CoverageStatus.Uncovered);
					currentFunction.addLine(currentLine);
				}
			}

		} catch (NumberFormatException e) {
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void createMarker(IFile cppFile, int lineNum, String message, String type) throws CoreException {
		Map attributes = new HashMap();
		MarkerUtilities.setMessage(attributes, message);
		MarkerUtilities.setLineNumber(attributes, lineNum);
		MarkerUtilities.createMarker(cppFile, attributes, type);
	}
}
