/*******************************************************************************
 * Copyright (c) 2010, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 * 
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package com.includator.tests.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;

public abstract class SourceFileTest extends BaseTestFramework implements ILogListener {
	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
	private static final String CONFIG_FILE_NAME_ENDING = ".config";

	protected TreeMap<String, TestSourceFile> fileMap;
	protected String fileWithSelection;
	protected TextSelection selection;
	protected String activeFileName;

	public SourceFileTest(String name, Vector<TestSourceFile> files) {
		super(name);
		fileMap = new TreeMap<String, TestSourceFile>();
		initActiveFileName(files);
		TestSourceFile configFile = null;
		for (TestSourceFile file : files) {
			fileMap.put(file.getName(), file);
			if (file.getName().endsWith(CONFIG_FILE_NAME_ENDING)) {
				configFile = file;
			}
		}
		initializeConfiguration(configFile);
	}

	private void initActiveFileName(Vector<TestSourceFile> files) {
		activeFileName = ".unknown";
		int index = 0;
		if (files.size() <= index) {
			return;
		}
		if (files.get(0).getName().endsWith(CONFIG_FILE_NAME_ENDING)) {
			index++;
		}
		if (files.size() > index) {
			activeFileName = files.get(index).getName();
		}
	}

	private void initializeConfiguration(TestSourceFile configFile) {

		Properties properties = new Properties();
		try {
			if (configFile != null) {
				properties.load(new ByteArrayInputStream(configFile.getSource().getBytes()));
				fileMap.remove(configFile.getName());
			}
		} catch (final IOException e) {
		}

		initCommonFields(properties);
		configureTest(properties);
	}

	protected void configureTest(Properties properties) {
	};

	private void initCommonFields(Properties properties) {
		String filename = properties.getProperty("filename", null);
		if (filename != null) {
			activeFileName = filename;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CTestPlugin.getDefault().getLog().addLogListener(this);
		for (TestSourceFile testFile : fileMap.values()) {
			if (testFile.getSource().length() > 0) {
				importFile(testFile.getName(), testFile.getSource());
			}
		}
	}

	@Override
	protected IFile importFile(String fileName, String contents) throws Exception {
		IFile file = project.getFile(fileName);
		IPath projectRelativePath = file.getProjectRelativePath();
		for (int i = projectRelativePath.segmentCount() - 1; i > 0; i--) {
			IPath folderPath = file.getProjectRelativePath().removeLastSegments(i);
			IFolder folder = project.getFolder(folderPath);
			if (!folder.exists()) {
				folder.create(false, true, monitor);
			}
		}
		return super.importFile(fileName, contents);
	}

	@Override
	protected void tearDown() throws Exception {
		fileManager.closeAllFiles();
		super.tearDown();
		cleanupProject();
	}

	@Override
	public void logging(IStatus status, String plugin) {
		Throwable ex = status.getException();
		StringBuffer stackTrace = new StringBuffer();
		if (ex != null) {
			stackTrace.append('\n');
			for (StackTraceElement ste : ex.getStackTrace()) {
				stackTrace.append(ste.toString());
			}
		}
		fail("Log-Message: " + status.getMessage() + stackTrace.toString());
	}

	public void setFileWithSelection(String fileWithSelection) {
		this.fileWithSelection = fileWithSelection;
	}

	public void setSelection(TextSelection selection) {
		this.selection = selection;
	}
}
