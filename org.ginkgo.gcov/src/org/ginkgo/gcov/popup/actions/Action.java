package org.ginkgo.gcov.popup.actions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class Action implements IObjectActionDelegate {

	private IProject project;

	/**
	 * Constructor for Action1.
	 */
	public Action() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		parse(null);
		parse2(null);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection == null) {
			return;
		}
		IStructuredSelection ss = (IStructuredSelection) selection;
		Object o = ss.getFirstElement();
		if (o instanceof IProject) {
			project = (IProject) o;
		} else if (o instanceof ICProject) {
			project = (IProject) ((ICProject) o).getAdapter(IProject.class);
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parse(IFile file1) {

		String[] cmdLine = { "lcov", "-c", "-d", ".", "-o", "app.info" };
		IPath workingDirectory = project.getLocation();

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

		String programName = cmdLine[0];
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
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		String errorText = process.getStreamsProxy().getErrorStreamMonitor()
				.getContents();
		System.out.println(errorText);

		String outputText = process.getStreamsProxy().getOutputStreamMonitor()
				.getContents();
		System.out.println(outputText);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parse2(IFile file1) {

		String[] cmdLine = { "genhtml", "-o", "doc", "-p", "`pwd`",
				"--num-space", "4", "-f", "app.info" };
		IPath workingDirectory = project.getLocation();
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

		String programName = cmdLine[0];
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
			/*********/
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			/*********/
		} catch (CoreException e) {
			e.printStackTrace();
		}

		String errorText = process.getStreamsProxy().getErrorStreamMonitor()
				.getContents();
		System.out.println(errorText);

		String outputText = process.getStreamsProxy().getOutputStreamMonitor()
				.getContents();
		System.out.println(outputText);
	}

}
