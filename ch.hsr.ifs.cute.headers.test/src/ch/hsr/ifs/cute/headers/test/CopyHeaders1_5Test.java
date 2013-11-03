/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.test;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.headers1_5.CuteHeaders_1_5;

public class CopyHeaders1_5Test extends CopyHeadersBaseTest {

	public CopyHeaders1_5Test(String m){
		super(m);
	}
	public final void testCopySuiteFiles() throws CoreException {
		CuteHeaders_1_5 h = new CuteHeaders_1_5();
		String suitename = "TestSuite";
		h.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
		assertSuiteFilesExist(suitename);
	}

}
