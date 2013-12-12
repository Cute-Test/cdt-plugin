/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package ch.hsr.ifs.cute.ui.test.fakebasetests;

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.pdom.CModelListener;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

@SuppressWarnings("restriction")
public class BaseTestCase {
	private static final String DEFAULT_INDEXER_TIMEOUT_SEC = "10";
	private static final String INDEXER_TIMEOUT_PROPERTY = "indexer.timeout";
	protected static final int INDEXER_TIMEOUT_SEC = Integer.parseInt(System.getProperty(INDEXER_TIMEOUT_PROPERTY, DEFAULT_INDEXER_TIMEOUT_SEC));
	protected static final int INDEXER_TIMEOUT_MILLISEC = INDEXER_TIMEOUT_SEC * 1000;

	@Rule
	public TestName name = new TestName();

	public static NullProgressMonitor npm() {
		return new NullProgressMonitor();
	}

	@Before
	public void setUp() throws Exception {
		CPPASTNameBase.sAllowRecursionBindings = false;
		CPPASTNameBase.sAllowNameComputation = false;
		CModelListener.sSuppressUpdateOfLastRecentlyUsed = true;
	}

	@After
	public void tearDown() throws Exception {
		ResourceHelper.cleanUp();
		TestScannerProvider.clear();
	}

	/**
	 * Some test steps need synchronizing against a CModel event. This class is a very basic means of doing that.
	 */
	static protected class ModelJoiner implements IElementChangedListener {
		private final boolean[] changed = new boolean[1];

		public ModelJoiner() {
			CoreModel.getDefault().addElementChangedListener(this);
		}

		public void clear() {
			synchronized (changed) {
				changed[0] = false;
				changed.notifyAll();
			}
		}

		public void join() throws CoreException {
			try {
				synchronized (changed) {
					while (!changed[0]) {
						changed.wait();
					}
				}
			} catch (InterruptedException e) {
				throw new CoreException(CCorePlugin.createStatus("Interrupted", e));
			}
		}

		public void dispose() {
			CoreModel.getDefault().removeElementChangedListener(this);
		}

		@Override
		public void elementChanged(ElementChangedEvent event) {
			// Only respond to post change events
			if (event.getType() != ElementChangedEvent.POST_CHANGE)
				return;

			synchronized (changed) {
				changed[0] = true;
				changed.notifyAll();
			}
		}
	}

	protected String getName() {
		return name.getMethodName();
	}

	public static void waitForIndexer(ICProject project) throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);

		final PDOMManager indexManager = CCoreInternals.getPDOMManager();
		assertTrue(indexManager.joinIndexer(INDEXER_TIMEOUT_SEC * 1000, npm()));
		long waitms = 1;
		while (waitms < 2000 && !indexManager.isProjectRegistered(project)) {
			Thread.sleep(waitms);
			waitms *= 2;
		}
		assertTrue(indexManager.isProjectRegistered(project));
		assertTrue(indexManager.joinIndexer(INDEXER_TIMEOUT_SEC * 1000, npm()));
	}

	public static void waitUntilFileIsIndexed(IIndex index, IFile file) throws Exception {
		TestSourceReader.waitUntilFileIsIndexed(index, file, INDEXER_TIMEOUT_SEC * 1000);
	}
}
