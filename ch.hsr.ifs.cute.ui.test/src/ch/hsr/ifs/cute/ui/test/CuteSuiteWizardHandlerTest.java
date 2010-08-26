package ch.hsr.ifs.cute.ui.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.headers.CuteHeaders_1_0;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.project.wizard.CuteSuiteWizardHandler;

public class CuteSuiteWizardHandlerTest extends TestCase {

	public CuteSuiteWizardHandlerTest(String m){
		super(m);
	}
	CuteSuiteWizardHandler cswh=null;
	private IFolder srcFolder;
	private IFolder cuteFolder;
	private IProject project;
	@SuppressWarnings("nls")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cswh=new CuteSuiteWizardHandler("theSuiteName"); //$NON-NLS-1$
		IWorkspaceRoot iwsr=ResourcesPlugin.getWorkspace().getRoot();
		project = iwsr.getProject("CSWHT");
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		srcFolder = project.getProject().getFolder("/src");
		srcFolder.create(true, true, new NullProgressMonitor());
		cuteFolder = project.getProject().getFolder("/cute");
		cuteFolder.create(true, true, new NullProgressMonitor());
	}
	
	public final void testAddTestFiles() {
		CuteHeaders_1_0 h = new CuteHeaders_1_0();
		addTestFiles(h);
	}

	@SuppressWarnings("nls")
	private void addTestFiles(ICuteHeaders cuteHeader ) {
		try{

			cswh.copyFiles(srcFolder,cuteHeader, cuteFolder);
			//for indirect reference, check dependencies
			
			IFile file=srcFolder.getFile("Test.cpp");
			if(file.exists()){
				file.delete(true, false, new NullProgressMonitor());
				assertFalse(file.exists());
			}
			IFile file1=srcFolder.getFile("suite.cpp");
			IFile file2=srcFolder.getFile("suite.h");
						
			assertTrue(file1.exists());
			assertTrue(file2.exists());
			//clean up
			
		}catch(CoreException ce){fail(ce.getMessage());}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, new NullProgressMonitor());
		project = null;
		cuteFolder = null;
		srcFolder = null;
	}

	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.CuteSuiteWizardHandler"); //$NON-NLS-1$
		ts.addTest(new CuteSuiteWizardHandlerTest("testAddTestFiles")); //$NON-NLS-1$
		return ts;
	}
}
