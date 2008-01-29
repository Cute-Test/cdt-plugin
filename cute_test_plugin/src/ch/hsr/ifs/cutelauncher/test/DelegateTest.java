package ch.hsr.ifs.cutelauncher.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import junit.framework.*;

public class DelegateTest extends TestCase {

	public DelegateTest(String m){
		super(m);
	}
	ILaunchConfigurationStub lcs;
	ch.hsr.ifs.cutelauncher.CuteLauncherDelegate cld;
	protected void setUp() throws Exception {
		super.setUp();
		cld=new ch.hsr.ifs.cutelauncher.CuteLauncherDelegate();
	}

	public final void testSourcelookupCustomPath() {
		lcs=new ILaunchConfigurationStub(true,"/someprj/src");
		IPath exePath=new org.eclipse.core.runtime.Path("c:/src/bin");
		IPath result=cld.sourcelookupPath(lcs, exePath);
		try{
			String rootpath=org.eclipse.core.runtime.Platform.getLocation().toOSString();
			String customSrcPath=lcs.getAttribute("customSrcPath","");
			String fileSeparator=System.getProperty("file.separator");
			IPath expected= new org.eclipse.core.runtime.Path(rootpath+customSrcPath+fileSeparator);
		assertEquals(expected, result);
		}catch(Exception e){fail(e.toString());}
	}
	public final void testSourcelookupDefaultPath(){
		lcs=new ILaunchConfigurationStub(false,"d:/src");
		try{
			assertEquals(false, lcs.getAttribute("",false));
		}catch(CoreException e){}
				
		IPath exePath=new org.eclipse.core.runtime.Path("D:/runtime-EclipseApplication/sourcePathTestingPrj/src/bin");
		IPath result=cld.sourcelookupPath(lcs, exePath);
		
		IPath expected=new org.eclipse.core.runtime.Path("D:/runtime-EclipseApplication/sourcePathTestingPrj/src");
		assertEquals(expected, result);
	}
	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.CuteLauncherDelegate");
		ts.addTest(new DelegateTest("testSourcelookupCustomPath"));
		ts.addTest(new DelegateTest("testSourcelookupDefaultPath"));
		return ts;
	}

}

class ILaunchConfigurationStub extends org.eclipse.debug.internal.core.LaunchConfiguration implements ILaunchConfiguration{ 
	final boolean v1;
	final String v2;
	public ILaunchConfigurationStub(boolean value1, String value2) {
		super(new org.eclipse.core.runtime.Path(""));
		v1=value1;v2=value2;
	}
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		return v1;
	}
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		return v2;
	}
	
}