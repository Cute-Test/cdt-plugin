package ch.hsr.ifs.cute.headers.v10.test;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.CuteHeaders_1_0;
import ch.hsr.ifs.cute.headers.base.test.CopyHeadersBaseTest;

public class CopyHeadersTest extends CopyHeadersBaseTest {

	public CopyHeadersTest(String m){
		super(m);
	}
	public final void testCopySuiteFiles() throws CoreException {
		CuteHeaders_1_0 h = new CuteHeaders_1_0();
		h.copySuiteFiles(srcFolder, new NullProgressMonitor(), "TestSuite", true); //$NON-NLS-1$
		checkSuiteFiles();
	}

}
