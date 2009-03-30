package ch.hsr.ifs.cutelauncher.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.ui.SuiteTemplateCopyUtil;
import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.project.wizard.CuteSuiteWizardHandler;
import ch.hsr.ifs.cute.ui.project.wizard.NewCuteSuiteWizardCustomPage;

public class CuteSuiteWizardHandlerTest extends TestCase {

	public CuteSuiteWizardHandlerTest(String m){
		super(m);
	}
	CuteSuiteWizardHandler cswh=null;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cswh=new CuteSuiteWizardHandler("theSuiteName");
	}

	@SuppressWarnings("nls")
	public final void testAddTestFiles() {
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
			
			cswh.copyFiles(srcFolder,getCuteHeader("Cute Headers 1.5.0"), cuteFolder);
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

	public final void testImplantActualsuitename() {
		try{
			Bundle bundle = TestPlugin.getDefault().getBundle();
			Path path = new Path("testDefs/SuiteWizTest.cpp");
			URL url=FileLocator.toFileURL(FileLocator.find(bundle, path, null));
			
			ByteArrayInputStream bais=SuiteTemplateCopyUtil.implantActualsuitename(url,"theSuiteName");
			
			Bundle bundle1 = TestPlugin.getDefault().getBundle();
			Path path1 = new Path("testDefs/SuiteWizTestResult.cpp");
			URL url1=FileLocator.toFileURL(FileLocator.find(bundle1, path1, null));
			BufferedInputStream bis=new BufferedInputStream(url1.openStream());
			
			if(bais.available()!=bis.available())fail("File size mismatch."+bais.available()+" "+bis.available());
			while(bis.available()>0){
				//System.out.println((char)bais.read()+" "+(char)bis.read());
				//System.out.println(bais.read()+" "+bis.read());
				assertEquals("different char detected",bais.read(), bis.read());
			}
		}catch(IOException e){fail(e.getMessage());}
	}
	public final void testNewCuteSuiteWizardCustomPageForValidIdentifier(){
		NewCuteSuiteWizardCustomPage ncsw=new NewCuteSuiteWizardCustomPage(null,null);

		Composite parent=new Composite(new org.eclipse.swt.widgets.Shell(),SWT.NO_FOCUS);
		ncsw.createControl(parent,false);

		
		ncsw.setSuiteName("**/invalidname");
		assertEquals("incorrect regex",ncsw.getSuiteName(),"suite");

		ncsw.setSuiteName("validname44");
		assertEquals("incorrect regex",ncsw.getSuiteName(),"validname44");

	}
	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.CuteSuiteWizardHandler");
		ts.addTest(new CuteSuiteWizardHandlerTest("testAddTestFiles"));
		ts.addTest(new CuteSuiteWizardHandlerTest("testImplantActualsuitename"));
		ts.addTest(new CuteSuiteWizardHandlerTest("testNewCuteSuiteWizardCustomPageForValidIdentifier"));
		return ts;
	}
}
