package ch.hsr.ifs.cute.headers.tests.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.versions.CuteHeaders2;


public class CopyHeaders2_2Test extends CopyHeadersBaseTest {

    public CopyHeaders2_2Test(String m) {
        super(m);
    }

    public final void testCopySuiteFiles221() throws CoreException {
        String suitename = "TestSuite";
        CuteHeaders2._2_1.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
        assertSuiteFilesExist(suitename);
    }

    public final void testCopySuiteFiles223() throws CoreException {
        String suitename = "TestSuite";
        CuteHeaders2._2_3.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
        assertSuiteFilesExist(suitename);
    }

    public final void testCopySuiteFiles225() throws CoreException {
        String suitename = "TestSuite";
        CuteHeaders2._2_5.copySuiteFiles(srcFolder, new NullProgressMonitor(), suitename, true);
        assertSuiteFilesExist(suitename);
    }
}
