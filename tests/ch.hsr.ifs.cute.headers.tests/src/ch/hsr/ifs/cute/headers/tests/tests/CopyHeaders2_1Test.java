package ch.hsr.ifs.cute.headers.tests.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.versions.CuteHeaders_2_1;


public class CopyHeaders2_1Test extends CopyHeadersBaseTest {

   public CopyHeaders2_1Test(String m) {
      super(m);
   }

   public final void testCopySuiteFiles() throws CoreException {
      CuteHeaders_2_1 h = new CuteHeaders_2_1();
      String suitename = "TestSuite";
      h.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
      assertSuiteFilesExist(suitename);
   }

}
