/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.test;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public abstract class CopyHeadersBaseTest extends TestCase {

	protected IFolder srcFolder;
	private IFolder cuteFolder;
	private IProject project;

	public CopyHeadersBaseTest() {
		super();
	}

	public CopyHeadersBaseTest(String name) {
		super(name);
	}

	@SuppressWarnings("nls")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IWorkspaceRoot iwsr=ResourcesPlugin.getWorkspace().getRoot();
		project = iwsr.getProject("CSWHT");
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		srcFolder = project.getProject().getFolder("/src");
		srcFolder.create(true, true, new NullProgressMonitor());
		cuteFolder = project.getProject().getFolder("/cute");
		cuteFolder.create(true, true, new NullProgressMonitor());
	}

	@SuppressWarnings("nls")
	protected void assertSuiteFilesExist(String suiteName) {
		assertFileExists(srcFolder, suiteName + ".cpp");
		assertFileExists(srcFolder, suiteName + ".h");
	}

	protected void assertFileExists(IContainer container, String fileName) {
		assertTrue(container.getFile(new Path(fileName)).exists());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, new NullProgressMonitor());
	}
}