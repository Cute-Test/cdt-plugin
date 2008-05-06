package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import ch.hsr.ifs.cutelauncher.ui.sourceactions.NewTestFunctionAction;

public class TestNewFunction extends Test1Skeleton {
	public TestNewFunction(String name) {
		super(name);
 	}
	public final void testNewTestFunctionAll(){
		ReadTestCase rtc=new ReadTestCase("testDefs/sourceActions/newTestfunction.txt");
		NewTestFunctionAction functionAction=new NewTestFunctionAction();
		for(int i=0;i<rtc.testname.size();i++){
			//if(i<rtc.testname.size()-1)continue;
			generateTest(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i),functionAction);
		}//skipped "at end2 with pushback duplicated" at position4 @see NewTestFunctionAction#createEdit 
	}
	public final static TestSuite generateNewFunctionTest(){
		TestSuite functorTS=new TestSuite("newTestFunction Tests");
		final ReadTestCase rtc1=new ReadTestCase("testDefs/sourceActions/newTestfunction.txt");
		final NewTestFunctionAction functionAction=new NewTestFunctionAction();
		for(int i=0;i<rtc1.testname.size();i++){
			final int j=i;
			String displayname=rtc1.testname.get(j).replaceAll("[()]", "*");//JUnit unable to display () as name
			junit.framework.TestCase test = new TestNewFunction("newTestFunction"+i+displayname) {
				@Override
				public void runTest() {
					generateTest(rtc1.testname.get(j),rtc1.test.get(j),rtc1.cursorpos.get(j).intValue(),rtc1.expected.get(j),functionAction);
				}
			};
			functorTS.addTest(test);
		}
		return functorTS;
	}
	
	public final void testProblemMarker(){
		ReadTestCase rtc=new ReadTestCase("testDefs/sourceActions/newTestfunctionMarker.txt");
		NewTestFunctionAction functionAction=new NewTestFunctionAction();
		int i=0;
		generateTest(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i),functionAction);
				
		IEditorInput editorInput = ceditor.getEditorInput();
		IFile editorFile = ((FileEditorInput)editorInput).getFile();
		
		boolean flag=false;
		IMarker[] problems = null;
		int depth = IResource.DEPTH_INFINITE;
		try {
		   problems = editorFile.findMarkers(IMarker.PROBLEM, true, depth);
		   
		   for(IMarker marker:problems){
			   String msg=(String)marker.getAttribute(IMarker.MESSAGE);
			   Map map=marker.getAttributes();
			  
			   if(msg!=null && msg.startsWith("cute:Duplicate Pushback name")){
				   int lineno=((Integer)marker.getAttribute(IMarker.LINE_NUMBER)).intValue();
				   assertEquals("pointer should be at line 3 based on test case:",3, lineno);
				   flag=true;
			   }
		   }
		} catch (CoreException e) {
			fail("CoreException"+e.getMessage());
		}
		assertTrue(flag);
		
	}
	
	public static TestSuite suite(boolean speedupMode){
		TestSuite result;
		if(speedupMode){
			result=new TestSuite("newTestFunction Tests");
			result.addTest(new TestNewFunction("testNewTestFunctionAll"));
		}else{
			result=generateNewFunctionTest();
		}
		
		result.addTest(new TestNewFunction("testProblemMarker"));
		return result;
	}
	
}
