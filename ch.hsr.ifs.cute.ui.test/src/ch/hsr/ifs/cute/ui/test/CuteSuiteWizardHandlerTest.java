/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cevelop.elevenator.Activator;
import com.cevelop.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.headers.headers1_7.CuteHeaders_1_7;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.project.wizard.CuteSuiteWizardHandler;

public class CuteSuiteWizardHandlerTest {

	CuteSuiteWizardHandler cswh = null;
	private IFolder srcFolder;
	private IFolder cuteFolder;
	private IProject project;
	private IScopeContext scope;
	private IEclipsePreferences node;

	@Before
	public void setUp() throws Exception {
		cswh = new CuteSuiteWizardHandler("theSuiteName");
		IWorkspaceRoot iwsr = ResourcesPlugin.getWorkspace().getRoot();
		project = iwsr.getProject("CSWHT");
		scope = new ProjectScope(project);
		node = scope.getNode(Activator.PLUGIN_ID);
		node.put("c_dialect", CPPVersion.CPP_14.toString());
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		srcFolder = project.getProject().getFolder("/src");
		srcFolder.create(true, true, new NullProgressMonitor());
		cuteFolder = project.getProject().getFolder("/cute");
		cuteFolder.create(true, true, new NullProgressMonitor());
	}

	@Test
	public void testAddTestFiles() throws CoreException {
		CuteHeaders_1_7 h = new CuteHeaders_1_7();
		addTestFiles(h);
		assertCuteHeaderFilesExist();
	}

	private void addTestFiles(ICuteHeaders cuteHeader) throws CoreException {
		cswh.copyExampleTestFiles(srcFolder, cuteHeader);

		IFile file = srcFolder.getFile("Test.cpp");
		if (file.exists()) {
			file.delete(true, false, new NullProgressMonitor());
			assertFalse(file.exists());
		}
	}

	private void assertCuteHeaderFilesExist() {
		IFile file1 = srcFolder.getFile("suite.cpp");
		IFile file2 = srcFolder.getFile("suite.h");

		assertTrue(file1.exists());
		assertTrue(file2.exists());
	}

	@After
	public void tearDown() throws Exception {
		project.delete(true, new NullProgressMonitor());
	}
}
