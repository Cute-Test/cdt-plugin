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
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.CuteNature;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author Emanuel Graf IFS
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CuteProjectWizardTest {

	private String cuteVersion;
	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
	}

	@SuppressWarnings("nls")
	@Test
	public void cuteProjectWizardHeaderIndex0() throws CoreException, BuildException {
		String projectName = executeProjectWizard(0, false, false);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject proj = workspace.getRoot().getProject(projectName);
		assertNotNull("No Project", proj);
		assertTrue(proj.hasNature(CuteNature.CUTE_NATURE_ID));
		ICuteHeaders header = UiPlugin.getCuteVersionString(proj);
		assertEquals(header.getVersionString(), cuteVersion);
		assertSrcFolderCreated(projectName, proj, "cute");
	}

	private void assertSrcFolderCreated(String projectName, IProject proj, String folderName) throws CoreException{
		IFolder cuteFolder =  proj.getFolder(folderName);
		assertNotNull(cuteFolder);
		ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(proj, false);
		checkSrcEntryExist(projectName, folderName, desc);
	}

	@SuppressWarnings("nls")
	private void checkSrcEntryExist(String projectName, String folderName, ICProjectDescription desc) {
		ICSourceEntry[] srcEntries = desc.getActiveConfiguration().getSourceEntries();
		boolean cuteSrcEntryFound = false;
		for (ICSourceEntry entry : srcEntries) {
			if(entry.getName().equals("/"+ projectName + "/"+ folderName)) {
				cuteSrcEntryFound = true;
			}
		}
		assertTrue(cuteSrcEntryFound);
	}

	@SuppressWarnings("nls")
	@Test
	public void cuteProjectWizardHeaderIndex1() throws CoreException {
		String projectName = executeProjectWizard(1, false, false);
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNotNull("No Project", proj);
		assertTrue(proj.hasNature(CuteNature.CUTE_NATURE_ID));
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
		assertSrcFolderCreated(projectName, proj, "boost");
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
		bot.waitUntil(Conditions.shellCloses(shell),10000);
		return projectName;
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
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

}
