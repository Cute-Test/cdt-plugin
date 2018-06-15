/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.tests.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.versions.CuteHeaders2;


public class CopyHeaders2_0Test extends CopyHeadersBaseTest {
   
   public CopyHeaders2_0Test(String m) {
      super(m);
   }
 
   public final void testCopySuiteFiles() throws CoreException {
      String suitename = "TestSuite";
      CuteHeaders2._0_1.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
      assertSuiteFilesExist(suitename);
   }
}
