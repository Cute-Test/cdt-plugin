package ch.hsr.ifs.cutelauncher.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;

import ch.hsr.ifs.cute.core.launch.CustomisedLaunchConfigTab;

public class SourceLookupPathTest extends TestCase {

	public SourceLookupPathTest(String m){
		super(m);
	}
	LaunchConfigurationStub lcs;
	ch.hsr.ifs.cute.core.launch.CuteLauncherDelegate cld;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cld=new ch.hsr.ifs.cute.core.launch.CuteLauncherDelegate();
	}

	public final void testSourcelookupCustomPath() {
		lcs=new LaunchConfigurationStub(true,"/someprj/src"); //$NON-NLS-1$
		IPath exePath=new org.eclipse.core.runtime.Path("c:/src/bin"); //$NON-NLS-1$
		IPath result=cld.sourcelookupPath(lcs, exePath);
		try{
			String rootpath=org.eclipse.core.runtime.Platform.getLocation().toOSString();
			String customSrcPath=lcs.getAttribute(CustomisedLaunchConfigTab.CUSTOM_SRC_PATH,""); //$NON-NLS-1$
			String fileSeparator=System.getProperty("file.separator"); //$NON-NLS-1$
			IPath expected= new org.eclipse.core.runtime.Path(rootpath+customSrcPath+fileSeparator);
		assertEquals(expected, result);
		}catch(Exception e){fail(e.toString());}
	}
	
	public final void testSourcelookupDefaultPath(){
		lcs=new LaunchConfigurationStub(false,"d:/src"); //$NON-NLS-1$
		try{
			assertEquals(false, lcs.getAttribute("",false)); //$NON-NLS-1$
		}catch(CoreException e){}
				
		IPath exePath=new org.eclipse.core.runtime.Path("D:/runtime-EclipseApplication/sourcePathTestingPrj/src/bin"); //$NON-NLS-1$
		IPath result=cld.sourcelookupPath(lcs, exePath);
		
		IPath expected=new org.eclipse.core.runtime.Path("D:/runtime-EclipseApplication/sourcePathTestingPrj/src"); //$NON-NLS-1$
		assertEquals(expected, result);
	}
	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.CuteLauncherDelegate"); //$NON-NLS-1$
		ts.addTest(new SourceLookupPathTest("testSourcelookupCustomPath")); //$NON-NLS-1$
		ts.addTest(new SourceLookupPathTest("testSourcelookupDefaultPath")); //$NON-NLS-1$
		return ts;
	}
}
//extend LaunchConfiguration as its constructor is protected 
class LaunchConfigurationStub extends org.eclipse.debug.internal.core.LaunchConfiguration implements ILaunchConfiguration{ 
	final boolean useCustomSrcPathProperty;
	final String customSrcPathProperty;
	public LaunchConfigurationStub(boolean value1, String value2) {
		super(new Path("")); //$NON-NLS-1$
		useCustomSrcPathProperty=value1;
		customSrcPathProperty=value2;
	}
	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		return useCustomSrcPathProperty;
	}
	@Override
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		return customSrcPathProperty;
	}
	
}