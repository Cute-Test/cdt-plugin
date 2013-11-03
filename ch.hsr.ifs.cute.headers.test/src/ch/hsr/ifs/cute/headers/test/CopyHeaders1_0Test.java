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

import ch.hsr.ifs.cute.headers.headers1_0.CuteHeaders_1_0;

public class CopyHeaders1_0Test extends CopyHeadersBaseTest {

	public CopyHeaders1_0Test(String m){
		super(m);
	}
	public final void testCopySuiteFiles() throws CoreException {
		CuteHeaders_1_0 h = new CuteHeaders_1_0();
		String suitename = "TestSuite";
		h.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
		assertSuiteFilesExist(suitename);
	}

}
