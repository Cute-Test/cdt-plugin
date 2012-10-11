/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.test.job;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.gcov.DeleteMarkerJob;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.test.mock.MockFile;

/**
 * @author Emanuel Graf IFS
 *
 */
public class DeleteMarkersTest extends TestCase {
	
	private static final String TEST_FILE_NAME = "testFile.cpp"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "project"; //$NON-NLS-1$

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		GcovPlugin.getDefault().getcModel().clearModel();
	}
	
	public void testDeleteJob() {
		IFile testFile = new MockFile(new Path(PROJECT_NAME + "/" + TEST_FILE_NAME)); //$NON-NLS-1$
		try {
			testFile.createMarker(GcovPlugin.PARTIALLY_MARKER_TYPE);
		} catch (CoreException e1) {
			fail(e1.getMessage());
		}
		DeleteMarkerJob job = new DeleteMarkerJob(testFile);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		IStatus result = job.getResult();
		assertEquals(result.getMessage(),IStatus.OK,result.getCode());
	}
	
	public void testDeleteJobResourceDoesNotExist() {
		IFile testFile = new MockFile(new Path(PROJECT_NAME + "/" + TEST_FILE_NAME), false); //$NON-NLS-1$
		try {
			testFile.createMarker(GcovPlugin.PARTIALLY_MARKER_TYPE);
		} catch (CoreException e1) {
			fail(e1.getMessage());
		}
		DeleteMarkerJob job = new DeleteMarkerJob(testFile);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		IStatus result = job.getResult();
		assertEquals(result.getMessage(),IStatus.OK,result.getSeverity());
	}

}
