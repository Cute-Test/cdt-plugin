package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import junit.framework.TestSuite;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.tests.text.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import ch.hsr.ifs.cutelauncher.ui.sourceactions.NewTestFunctionActionDelegate;

public class TestBugFixes extends Test1Skeleton {
	public TestBugFixes(String name) {
		super(name);
 	}
	
	public void testNewTestFunctionWithBadLocationException(){
		ReadTestCase rtc=new ReadTestCase("testDefs/sourceActions/bugfix.cpp",false);

		Integer[] c=rtc.parseForMultiCursorPosition();
		Integer[] cursorpos={c[0],261};
		rtc.removeCaretFromTest();
		String testSrcCode=rtc.test.get(0);
		
		try{
			IFile inputFile=MemoryBaseTestFramework.importFile("A.cpp",testSrcCode);
			IEditorPart editor= EditorTestHelper.openInEditor(inputFile, true);
			assertNotNull(editor);
			assertTrue(editor instanceof CEditor);
			ceditor=(CEditor)editor;
			
			//set cursor pos 
			ISelectionProvider selectionProvider=ceditor.getSelectionProvider();
			selectionProvider.setSelection(new TextSelection(cursorpos[0], 0));
			
			NewTestFunctionActionDelegate ntfad=new NewTestFunctionActionDelegate();
			ntfad.run(null);
			
			LinkedModeUI linked=ntfad.testOnlyGetLinkedMode();
			org.eclipse.jface.text.IRegion region=linked.getSelectedRegion();
			System.out.println("1st pos "+region.getOffset()+" "+region.getLength());
			getResult(editor, region);
			
			final java.lang.reflect.Method[] methods = LinkedModeUI.class.getDeclaredMethods();
		    for (int i = 0; i < methods.length; ++i) {
		      if (methods[i].getName().equals("next")) {
		        final Object params[] = {};
		        methods[i].setAccessible(true);
		        Object ret = methods[i].invoke(linked, params);
		        //System.out.println(ret);
		        //assertEquals("Selecting the first object in tree",ret.toString(),firstObjName);
		      }
		    }
			
		    region=linked.getSelectedRegion();
			System.out.println("next "+region.getOffset()+" "+region.getLength());
			getResult(editor, region);
			
//			System.out.println("Setting cursor position 2 "+cursorpos[1]);
			selectionProvider.setSelection(new TextSelection(cursorpos[1], 0));
			NewTestFunctionActionDelegate ntfad1=new NewTestFunctionActionDelegate();
			ntfad1.run(null);
						
			linked=ntfad.testOnlyGetLinkedMode();
			region=linked.getSelectedRegion();
			System.out.println("before "+region.getOffset()+" "+region.getLength());
			
			
			getResult(editor, region);
			
//			IWorkbenchWindow workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//			
//			IWorkbenchPart part = page.getActivePartReference().getPart(true);
//			
//			// Create a fake PopupMenuExtender so we can get some data back.
//			final MenuManager fakeMenuManager = new MenuManager();
//			fakeMenuManager.add(new GroupMarker(
//					org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
//			final PopupMenuExtender extender = new PopupMenuExtender(null,
//					fakeMenuManager, selectionProvider, part);
//			
//			extender.menuAboutToShow(fakeMenuManager);
//
//			fakeMenuManager.setVisible(true);
//			
//			Thread.sleep(3000);
//			
//			extender.dispose();
//
//			// Check to see if the appropriate object contributions are present.
//			final IContributionItem[] items = fakeMenuManager.getItems();
			
						
//			IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
			
			System.out.println();
			
			
		}catch(Exception e){e.printStackTrace();fail("\n"+e.getMessage());}
		
	}

	private void getResult(IEditorPart editor,
			org.eclipse.jface.text.IRegion region) {
		Object ele=(editor).getEditorInput();
		IDocumentProvider idp=ceditor.getDocumentProvider();
		IDocument fDocument= idp.getDocument(ele);
		String results=fDocument.get();
//		System.out.println(results);
		
		System.out.println(region.getOffset()+" "+region.getLength());
		System.out.println("KKK\n"+results.substring(region.getOffset(),region.getOffset()+region.getLength()));
	}
	
	public static TestSuite suite(){
		TestSuite functorTS=new TestSuite("bug fix Tests");
		functorTS.addTest(new TestBugFixes("testNewTestFunctionWithBadLocationException"));
		return functorTS;
	
	}
	
}
