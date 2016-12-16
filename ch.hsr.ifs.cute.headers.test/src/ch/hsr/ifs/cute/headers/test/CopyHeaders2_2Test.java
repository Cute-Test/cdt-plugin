package ch.hsr.ifs.cute.headers.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.headers2_2.CuteHeaders_2_2;

public class CopyHeaders2_2Test extends CopyHeadersBaseTest{
		
		public CopyHeaders2_2Test(String m){
			super(m);
		}
		
		public final void testCopySuiteFiles() throws CoreException {
			CuteHeaders_2_2 h = new CuteHeaders_2_2();
			String suitename = "TestSuite";
			h.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
			assertSuiteFilesExist(suitename);
		}

}
