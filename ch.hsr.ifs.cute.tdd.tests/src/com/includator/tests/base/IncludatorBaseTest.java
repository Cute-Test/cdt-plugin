/*******************************************************************************
 * Copyright (c) 2010, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved.
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package com.includator.tests.base;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public abstract class IncludatorBaseTest extends SourceFileTest {

	private final List<String> externalIncudeDirPaths;
	private final List<String> inProjectIncudeDirPaths;
	private final Set<String> includePathsSubDirs;
	private final List<String> includeSubstitutionLoaderFoldersPaths;

	public IncludatorBaseTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
		externalIncudeDirPaths = new ArrayList<String>();
		inProjectIncudeDirPaths = new ArrayList<String>();
		includePathsSubDirs = new LinkedHashSet<String>();
		includeSubstitutionLoaderFoldersPaths = new ArrayList<String>();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpIndex();
		checkTestStatus();
	}

	private void setUpIndex() throws CoreException, InterruptedException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, NULL_PROGRESS_MONITOR);
		IndexerPreferences.set(project.getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, Boolean.TRUE.toString());
		IndexerPreferences.set(project.getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, Boolean.TRUE.toString());
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);

		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, NULL_PROGRESS_MONITOR);

		boolean joined = CCorePlugin.getIndexManager().joinIndexer(20000, NULL_PROGRESS_MONITOR);
		if (!joined) {
			// Second join due to some strange interruption of JobMonitor when starting unit tests.
			System.err.println("First join on indexer failed. Trying again.");
			joined = CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, NULL_PROGRESS_MONITOR);
			assertTrue("The indexing operation of the test CProject has not finished jet. This should not happen...", joined);
		}
	}

	private void checkTestStatus() throws CoreException {
		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		try {
			index.acquireReadLock();
			boolean hasFiles = index.getAllFiles().length != 0;
			if (!hasFiles) {
				System.err.println("Test " + getName() + " is not properly setup and will most likely fail!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			index.releaseReadLock();
		}
	}


	@Override
	protected void tearDown() throws Exception {
		TestScannerProvider.clear();
		closeOpenEditors();
		super.tearDown();
	}

	private void closeOpenEditors() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

	protected void addIncludeDirPath(String path) {
		externalIncudeDirPaths.add(path);
	}

	protected void addInProjectIncludeDirPath(String projectRelativePath) {
		inProjectIncudeDirPaths.add(projectRelativePath);
	}

	protected String makeProjectAbsolutePath(String relativePath) {
		IPath projectPath = project.getLocation();
		return projectPath.append(relativePath).toOSString();
	}

	protected void addIncludeSubstitutionFolder(String folderPath) {
		includeSubstitutionLoaderFoldersPaths.add(folderPath);
	}

	protected void addIncludePathsSubDir(String includePathsSubDir) {
		includePathsSubDirs.add(includePathsSubDir);
	}
}