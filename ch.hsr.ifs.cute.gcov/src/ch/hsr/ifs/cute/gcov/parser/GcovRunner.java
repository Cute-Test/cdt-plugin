/*******************************************************************************
 * Copyright (c) 2007-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.parser;

import static ch.hsr.ifs.cute.gcov.util.ProjectUtil.getConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;

import ch.hsr.ifs.cute.gcov.GcovPlugin;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class GcovRunner {
	public static void runGcov(String filePath, File workingDirectory, IProject project) throws CoreException {
		String[] cmdLine;
		if (runningCygwin(project)) {
			cmdLine = getCygwinGcovCommand(filePath);
		} else {
			cmdLine = getGcovCommand(filePath);
		}

		String[] envp = getEnvironmentVariables(project);
		Process p = DebugPlugin.exec(cmdLine, workingDirectory, envp);
		String programName = cmdLine[0];
		Map<String, String> processAttributes = new HashMap<String, String>();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);

		if (p != null) {
			final Launch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
			IProcess process = DebugPlugin.newProcess(launch, p, programName, processAttributes);
			if (process == null) {
				p.destroy();
				GcovPlugin.log("Gcov Process is null");
			} else {
				//TODO: tcorbat: Why not p.waitFor()?
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
			GcovPlugin.log("Could not create gcov process");
		}
	}

	private static String[] getGcovCommand(String iPath) {
		String[] cmdLine = { "gcov", "-f", "-b", iPath };
		return cmdLine;
	}

	private static boolean runningCygwin(IProject project) {
		final IConfiguration config = getConfiguration(project);
		if (config != null) {
			final IToolChain toolChain = config.getToolChain();
			if (toolChain != null) {
				return toolChain.getName().startsWith("Cygwin");
			}
		}
		return false;
	}

	private static String[] getEnvironmentVariables(IProject project) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		final IEnvironmentVariableProvider environmentVariableProvider = ManagedBuildManager.getEnvironmentVariableProvider();
		IEnvironmentVariable[] variables = environmentVariableProvider.getVariables(info.getDefaultConfiguration(), true);
		String[] variableStrings = new String[variables.length];
		for (int i = 0; i < variableStrings.length; ++i) {
			variableStrings[i] = variables[i].toString();
		}
		return variableStrings;
	}

	private static String[] getCygwinGcovCommand(String iPath) {
		String[] cmdLine = { "sh", "-c", "'gcov", "-f", "-b", iPath + "'" };
		return cmdLine;
	}
}
