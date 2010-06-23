package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.TestSuite;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import ch.hsr.ifs.cute.ui.sourceactions.NewTestFunctionActionDelegate;

@SuppressWarnings("restriction")
public class TestBugFixes //extends Test1Skeleton {
	extends org.eclipse.cdt.core.tests.BaseTestFramework{
	
	private static final String TEST_DEFS = "testDefs/cute/sourceActions/bugfix.cpp";
	public TestBugFixes(String name) {
		super(name);
 	}
	protected static CEditor ceditor;
	public void testNewTestFunctionhighlight(){
		ReadTestCase rtc=new ReadTestCase(TEST_DEFS,false);

		Integer[] cursorpos={212,261};
		rtc.removeCaretFromTest();
		String testSrcCode=rtc.test.get(0);
		
		try{
			//IFile inputFile=MemoryBaseTestFramework.importFile("A.cpp",testSrcCode);
			IFile inputFile=importFile("A.cpp",testSrcCode);
			IEditorPart editor= EditorTestHelper.openInEditor(inputFile, true);
			assertNotNull(editor);
			assertTrue(editor instanceof CEditor);
			ceditor=(CEditor)editor;
			ISelectionProvider selectionProvider=ceditor.getSelectionProvider();
			
			selectionProvider.setSelection(new TextSelection(cursorpos[0], 0));
			
			NewTestFunctionActionDelegate ntfad=new NewTestFunctionActionDelegate();
			ntfad.run(null);
			
			ntfad.testOnlyGetLinkedMode();

			//set cursor location to be at the newly created newTest^Function
			selectionProvider.setSelection(new TextSelection(cursorpos[1], 0));
			ntfad.run(null);
			
			LinkedModeUI linked2ndCopy=ntfad.testOnlyGetLinkedMode();
			linked2ndCopy.getSelectedRegion();
			
			//leave(ILinkedModeListener.UPDATE_CARET);
			//default access, so need reflection
			boolean flag=true;
			final java.lang.reflect.Method[] methods = LinkedModeUI.class.getDeclaredMethods();
		    for (int i = 0; i < methods.length; ++i) {
		      if (methods[i].getName().equals("leave")) {
		        final Object params[] = {ILinkedModeListener.UPDATE_CARET};
		        methods[i].setAccessible(true);
		        methods[i].invoke(linked2ndCopy, params);
		        //System.out.println(ret);
		        //assertEquals("Selecting the first object in tree",ret.toString(),firstObjName);
		        flag=false;
		      }
		    }
			assertFalse(flag);
		    
			String results = getText(editor);
			
//			prettyPrintRegion(region);
//			System.out.println("KKK\n"+results.substring(region.getOffset(),region.getOffset()+region.getLength()));    
			
			ISelection see=selectionProvider.getSelection();
			TextSelection selection = (TextSelection) see;
//			System.out.println("texteditor:"+selection.getOffset()+" "+selection.getLength());
			
			results = getText(editor);
//			System.out.println("["+results.substring(selection.getOffset(),selection.getOffset()+selection.getLength())+"]");
			results=results.substring(selection.getOffset(),selection.getOffset()+selection.getLength());
			
			String expected="ASSERTM(\"start writing tests\", false);";
			assertEquals("",expected,results);
			
		}catch(Exception e){e.printStackTrace();fail("\n"+e.getMessage());}
		
	}

	private String getText(IEditorPart editor) {
		Object ele=(editor).getEditorInput();
		IDocumentProvider idp=ceditor.getDocumentProvider();
		IDocument fDocument= idp.getDocument(ele);
		return fDocument.get();
	}
	
	public static TestSuite suite(){
		TestSuite ts=new TestSuite("bug fix Tests");
		ts.addTest(new TestBugFixes("testNewTestFunctionhighlight"));
		return ts;
	}
	@Override
	protected void tearDown () throws Exception {
		EditorTestHelper.closeEditor(ceditor);
		/*if (fCProject != null)
			CProjectHelper.delete(fCProject);
		if (fNonCProject != null) {
			ResourceHelper.delete(fNonCProject);
		}*/
		super.tearDown();
	}
	
}



			
//IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);