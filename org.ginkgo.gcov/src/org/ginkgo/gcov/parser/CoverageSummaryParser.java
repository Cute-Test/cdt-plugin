package org.ginkgo.gcov.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.ginkgo.gcov.GcovPlugin;
import org.ginkgo.gcov.builder.ICoverageListener;
import org.ginkgo.gcov.model.CoverageData;
import org.ginkgo.gcov.model.DemangleHelper;

public class CoverageSummaryParser implements IParser {

	private Pattern funcPattern;
	private Pattern filePattern;
	private Pattern coveragePattern;
	private String consoleName = "gcov console";
	ArrayList<ICoverageListener> listeners = null;

	public CoverageSummaryParser() {
		// gcov adds sometimes certain signs to the function name and the
		// following regular expression should filter them out
		// funcPattern = Pattern
		// .compile("Function `?([_Z][A-Za-z]*[0-9]+)([a-z].+)?v'");
		funcPattern = Pattern.compile("Function `?([_Z][A-Za-z]*[0-9]+)([a-z].+)?v'");
		filePattern = Pattern.compile("File `(.+)'");
		coveragePattern = Pattern.compile("Lines executed:(.+)% of (.+)$");
	}

	private MessageConsole getConsole() {
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		IConsole[] consoles = manager.getConsoles();
		MessageConsole messageConsole = null;
		for (IConsole console : consoles) {
			if (console.getName().equals(consoleName)) {
				messageConsole = (MessageConsole) console;
				break;
			}
		}
		if (messageConsole == null) {
			messageConsole = new MessageConsole(consoleName, null);
			manager.addConsoles(new IConsole[] { messageConsole });
		}
		return messageConsole;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parse(IFile file) {
		String[] cmdLine = { "gcov", "-f", "-b", file.getName()};

		IPath workingDirectory = file.getParent().getLocation();
		File workingDir = null;
		if (workingDirectory != null) {
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

		String programName = "gcov";// cmdLine[2]; // zeigt zu gcov
		Map processAttributes = new HashMap();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);

		if (p != null) {
			process = DebugPlugin.newProcess(new Launch(null,
					ILaunchManager.RUN_MODE, null), p, programName,
					processAttributes);
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
		MessageConsole console = getConsole();
		String cmd = generateCommandLine(cmdLine);
		process.setAttribute(IProcess.ATTR_CMDLINE, cmd);

		printCommand(console, cmd);

		String errorText = process.getStreamsProxy().getErrorStreamMonitor()
				.getContents();
		printError(console, errorText);

		String outputText = process.getStreamsProxy().getOutputStreamMonitor()
				.getContents();
		printOutput(console, outputText);
		parseString(outputText, file.getProject(), file.getLocalTimeStamp());
	}

	private void printOutput(MessageConsole console, String outputText) {
		MessageConsoleStream outputStream = console.newMessageStream();
		outputStream.setActivateOnWrite(false);
		outputStream.println(outputText);
	}

	private void printError(MessageConsole console, String errorText) {
		MessageConsoleStream errorStream = console.newMessageStream();
		errorStream.setActivateOnWrite(true);
		if (errorText != null && !errorText.equals("")) {
			errorStream.println(errorText);
		}
	}

	private void printCommand(MessageConsole console, String cmd) {
		MessageConsoleStream commandStream = console.newMessageStream();
		commandStream.setActivateOnWrite(false);

		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		commandStream.setColor(new Color(display, 0, 0, 255));

		commandStream.print(">");
		commandStream.println(cmd);
	}

	private void parseString(String outputText, IProject project, long timeStamp) {
		BufferedReader in = new BufferedReader(new StringReader(outputText));
		String line = null;
		CoverageData cov = new CoverageData();
		cov.setTimeStamp(new Timestamp(timeStamp));

		try {
			while ((line = in.readLine()) != null) {
				Matcher matcher = funcPattern.matcher(line);
				if (matcher.find()) {
					cov.setElementType("Function");
					String functionName = matcher.group(2);
					functionName = DemangleHelper.demangle(functionName);
					cov.setElementName(functionName);
					continue;
				}
				matcher = filePattern.matcher(line);
				if (matcher.find()) {
					cov.setElementType("File");
					String[] ps = matcher.group(1).split("/");

					cov.setElementName(ps[ps.length - 1]);

					continue;
				}
				matcher = coveragePattern.matcher(line);
				if (matcher.find()) {
					cov.setPersent(matcher.group(1));
					cov.setTotalLine(matcher.group(2));
					continue;
				}
				if (line.equals("")) {
					ArrayList<ICoverageListener> a;
					a = getListeners();
					for (int i = 0; i < a.size(); i++) {
						a.get(i).addCoverageData(project, cov);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<ICoverageListener> getListeners() {
		if (listeners != null) {

		} else {
			listeners = new ArrayList<ICoverageListener>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();

			IExtensionPoint point = registry.getExtensionPoint(GcovPlugin
					.getDefault().getBundle().getSymbolicName()
					+ ".listeners");

			IExtension[] extensions = point.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i]
						.getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					if (elements[j].getName().equals("provider")) {
						try {
							ICoverageListener provider = (ICoverageListener) elements[j]
									.createExecutableExtension("class");
							listeners.add(provider);

						} catch (CoreException ex) {
							ex.printStackTrace();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
		return listeners;
	}

	private String generateCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < commandLine.length; i++) {
			buf.append(' ');
			char[] characters = commandLine[i].toCharArray();
			StringBuffer command = new StringBuffer();
			boolean containsSpace = false;
			for (int j = 0; j < characters.length; j++) {
				char character = characters[j];
				if (character == '\"') {
					command.append('\\');
				} else if (character == ' ') {
					containsSpace = true;
				}
				command.append(character);
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command);
				buf.append('\"');
			} else {
				buf.append(command);
			}
		}
		return buf.toString();
	}

	public static void deleteSummary(IProject project) {
		try {
			project.accept(new IResourceVisitor() {
				
				public boolean visit(IResource resource) throws CoreException {
					IProject project = resource.getProject();
					try {
						// String n = resource.getName();
						String n = resource.getFullPath().toOSString();
						project.setPersistentProperty(new QualifiedName(n,
								"persent"), null);
						project.setPersistentProperty(new QualifiedName(n,
								"totalLine"), null);
					} catch (CoreException e) {

					}

					return true;
				}
			});
		} catch (CoreException ce) {
		}

	}
}
