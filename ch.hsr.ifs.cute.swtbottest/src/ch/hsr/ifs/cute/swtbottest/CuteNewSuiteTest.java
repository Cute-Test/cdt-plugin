/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil, Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.swtbottest;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Emanuel Graf IFS
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CuteNewSuiteTest {

	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
	}

	@SuppressWarnings("nls")
	@Before
	public void setup() throws CoreException, IOException {
		bot.perspectiveByLabel("C/C++").activate();
		bot.menu("File").menu("Import...").click();
		final SWTBotShell shell = bot.shell("Import");
		shell.activate();
		SWTBotTreeItem expandNode = bot.tree().expandNode("General");
		expandNode.select("Existing Projects into Workspace");
		bot.button("Next >").click();
		shell.pressShortcut(SWT.ALT, 'a');
		URL location = Activator.getDefault().getBundle().getResource("cuteTestProject.zip");
		location = FileLocator.toFileURL(location);
		System.out.println(location);
		System.out.println(location.getPath());
		bot.text(1).setText(location.getPath());
		shell.pressShortcut(SWT.ALT, 'e');
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell),10000);
		CCorePlugin.getIndexManager().joinIndexer(50000, new NullProgressMonitor());
	}

	@SuppressWarnings("nls")
	@Test
	public void newSuiteTest() throws Exception {
		SWTBot projBot = bot.viewByTitle("Project Explorer").bot();
		SWTBotTreeItem tItem = projBot.tree().expandNode("cute").expandNode("src").getNode("Test.cpp");
		tItem.doubleClick();
		tItem.select();
		bot.menu("File").menu("New").menu("Suite File").click();
		final SWTBotShell shell = bot.shell("New Cute Suite File");
		shell.activate();
		bot.checkBox().click();
		bot.text(1).setText("TestSuite");
		assertTrue(bot.comboBox().itemCount() >= 1);
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell), 20000);
		ICProject proj = CCorePlugin.getDefault().getCoreModel().create(ResourcesPlugin.getWorkspace().getRoot().getProject("cute"));
		assertTrue(proj.exists());
		IProject project = proj.getProject();
		IFile file = project.getFile("src/TestSuite.h");
		assertTrue(file.exists());
		file = project.getFile("src/TestSuite.cpp");
		assertTrue(file.exists());
		file = project.getFile("src/Test.cpp");
		assertTrue(file.exists());
		file.refreshLocal(IResource.DEPTH_INFINITE,null);
		String content = getCodeFromIFile(file);
		assertTrue(content.contains("#include \"TestSuite.h\""));
		assertTrue(content.contains("cute::suite TestSuite = make_suite_TestSuite();"));
		assertTrue(content.contains("cute::makeRunner(lis)(TestSuite, \"TestSuite\");"));
	}

	private String getCodeFromIFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		br.close();
		return code.toString();
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
