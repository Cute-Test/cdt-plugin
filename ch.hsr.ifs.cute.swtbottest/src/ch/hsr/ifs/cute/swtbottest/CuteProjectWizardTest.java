/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil, Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.swtbottest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author Emanuel Graf IFS
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CuteProjectWizardTest {

	private final class JoinableMonitor extends NullProgressMonitor {
		public boolean isDone = false;

		@Override
		public synchronized void done() {
			isDone = true;
			notifyAll();
		}

		public synchronized void join() {
			while(!isDone) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final class ShellOpenCondition extends DefaultCondition {
		private final SWTBotShell shell;

		private ShellOpenCondition(SWTBotShell shell) {
			this.shell = shell;
		}

		@Override
		public boolean test() throws Exception {

			return !shell.isActive();
		}

		@Override
		public String getFailureMessage() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static SWTWorkbenchBot bot;
	private String cuteVersion;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		//		bot.viewByTitle("Welcome").close(); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Test
	public void cuteProjectWizardHeaderIndex0() throws CoreException {
		String projectName = executeProjectWizard(0, false, false);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject proj = workspace.getRoot().getProject(projectName);
		assertNotNull("No Project", proj);
		IFolder cuteFolder =  proj.getFolder("cute");
		assertNotNull(cuteFolder);
		ICuteHeaders header = UiPlugin.getCuteVersionString(proj);
		assertEquals(header.getVersionString(), cuteVersion);
	}

	@SuppressWarnings("nls")
	@Test
	public void cuteProjectWizardHeaderIndex1() throws CoreException {
		String projectName = executeProjectWizard(1, false, false);
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNotNull("No Project", proj);
		IFolder cuteFolder =  proj.getFolder("cute");
		assertNotNull(cuteFolder);
		ICuteHeaders header = UiPlugin.getCuteVersionString(proj);
		assertEquals(header.getVersionString(), cuteVersion);
	}

	@SuppressWarnings("nls")
	@Test
	public void cuteProjectWizardHeaderCopyBoost() throws CoreException {
		String projectName = executeProjectWizard(0, true, false);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject proj = workspace.getRoot().getProject(projectName);
		assertNotNull("No Project", proj);
		IFolder boost = proj.getFolder("boost");
		assertNotNull(boost);
		ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(proj, false);
		desc.getActiveConfiguration().getFolderDescriptions();
	}

	@SuppressWarnings("nls")
	private String executeProjectWizard(int headerIndex, boolean copyBoost, boolean enableGcov) {
		bot.perspectiveByLabel("C/C++").activate();
		bot.menu("File").menu("New").menu("Project...").click();
		final SWTBotShell shell = bot.shell("New Project");
		shell.activate();
		bot.tree().expandNode("C/C++").select("C++ Project");
		bot.button("Next >").click();
		String projectName = "CuteProject";
		bot.textWithLabel("Project name:").setText(projectName);
		SWTBotTree swtTree = bot.tree();
		bot.checkBox("Show project types and toolchains only if they are supported on the platform").click();
		swtTree.select("Cute Project");
		bot.button("Next >").click();
		bot.comboBox().setSelection(headerIndex);
		cuteVersion = bot.comboBox().selection();
		if(copyBoost) {
			bot.checkBox("Copy Boost headers into Project").click();
		}
		if(enableGcov) {
			bot.checkBox("Enable coverage analysis using gcov").click();
		}
		bot.button("Finish").click();
		ICondition cond = new ShellOpenCondition(shell);
		cond.init(bot);
		bot.waitUntil(cond);
		return projectName;
	}

	@After
	public void cleanup() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (IProject project : projects) {
			JoinableMonitor monitor = new JoinableMonitor();
			project.delete(true, monitor);
			monitor.join();
		}
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}

}
