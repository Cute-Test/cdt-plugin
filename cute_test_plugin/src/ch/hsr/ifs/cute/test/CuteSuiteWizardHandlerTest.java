package ch.hsr.ifs.cute.test;

import java.util.SortedSet;

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

import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.project.wizard.CuteSuiteWizardHandler;

public class CuteSuiteWizardHandlerTest extends TestCase {

	public CuteSuiteWizardHandlerTest(String m){
		super(m);
	}
	CuteSuiteWizardHandler cswh=null;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cswh=new CuteSuiteWizardHandler("theSuiteName"); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	public final void testAddTestFiles1_5() {
		String cuteVersion = "Cute Headers 1.5.0";
		addTestFiles(cuteVersion);
	}
	
	@SuppressWarnings("nls")
	public final void testAddTestFiles1_6() {
		String cuteVersion = "Cute Headers 1.6.0";
		addTestFiles(cuteVersion);
	}
	
	@SuppressWarnings("nls")
	public final void testAddTestFiles1_0() {
		String cuteVersion = "Cute Headers 1.0.0";
		addTestFiles(cuteVersion);
	}

	@SuppressWarnings("nls")
	private void addTestFiles(String cuteVersion) {
		try{
			IWorkspaceRoot iwsr=ResourcesPlugin.getWorkspace().getRoot();
//			IWorkspace iws=ResourcesPlugin.getWorkspace();
//			IProjectDescription prjdesc=iws.newProjectDescription("CSWHT");
			IProject prj=iwsr.getProject("CSWHT");
			prj.create(new NullProgressMonitor());
			prj.open(new NullProgressMonitor());
			IFolder srcFolder= prj.getProject().getFolder("/src");
			srcFolder.create(true, true, new NullProgressMonitor());
			IFolder cuteFolder= prj.getProject().getFolder("/cute");
			cuteFolder.create(true, true, new NullProgressMonitor());
			
			cswh.copyFiles(srcFolder,getCuteHeader(cuteVersion), cuteFolder);
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
			file1.delete(true, false, new NullProgressMonitor());
			file2.delete(true, false, new NullProgressMonitor());
			prj.delete(true, new NullProgressMonitor());
		}catch(CoreException ce){fail(ce.getMessage());}
	}
	
	private ICuteHeaders getCuteHeader(String version) {
		SortedSet<ICuteHeaders> headers = UiPlugin.getInstalledCuteHeaders();
		for (ICuteHeaders cuteHeaders : headers) {
			if(version.equals(cuteHeaders.getVersionString()))
				return cuteHeaders;
		}
		
		return null;
	}

	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.CuteSuiteWizardHandler"); //$NON-NLS-1$
		ts.addTest(new CuteSuiteWizardHandlerTest("testAddTestFiles1_0")); //$NON-NLS-1$
		ts.addTest(new CuteSuiteWizardHandlerTest("testAddTestFiles1_5")); //$NON-NLS-1$
		ts.addTest(new CuteSuiteWizardHandlerTest("testAddTestFiles1_6")); //$NON-NLS-1$
		return ts;
	}
}
