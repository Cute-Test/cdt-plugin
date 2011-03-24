/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.v15.test;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.CuteHeaders_1_5;
import ch.hsr.ifs.cute.headers.base.test.CopyHeadersBaseTest;

public class CopyHeadersTest extends CopyHeadersBaseTest {

	public CopyHeadersTest(String m){
		super(m);
	}
	public final void testCopySuiteFiles() throws CoreException {
		CuteHeaders_1_5 h = new CuteHeaders_1_5();
		String suitename = "TestSuite"; //$NON-NLS-1$
		h.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
		checkSuiteFiles(suitename);
	}

}
