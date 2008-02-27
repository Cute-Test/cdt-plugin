package ch.hsr.ifs.cutelauncher.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.cdt.internal.core.model.ext.SourceRange;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.tests.text.Accessor;
import org.eclipse.cdt.ui.tests.text.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cutelauncher.ui.sourceactions.NewTestFunctionAction;

public class SourceActionsTest extends BaseTestFramework {

	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
	public SourceActionsTest(String name) {
		super(name);
 	}
	ReadTestCase rtc;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		rtc=new ReadTestCase();
	}
	private static CEditor ceditor;
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
	
	class mySourceRange extends SourceRange{//for setting current cursor position
		public mySourceRange(int offset){
			super(offset,0);
		}
	}
	
	public final void testNewTestFunctionAll(){
		for(int i=0;i<4;i++){
			testNewTestFunction(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i));
		}//skipped "at end2 with pushback duplicated" at position4 
	}
	
	// org.eclipse.cdt.ui.tests/ui/org.eclipse.cdt.ui.tests.text/BasicCeditor
	//@see org.eclipse.cdt.ui.tests.text.BasicCeditorTest#setUpEditor
	public final void testNewTestFunction(String testname,String testSrcCode, int cursorpos, String expectedOutput){
		try{
		IFile inputFile=importFile("A.cpp",testSrcCode);
		/*ITranslationUnit tu= CoreModelUtil.findTranslationUnit(inputFile);
		
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());	
		IASTTranslationUnit astTu = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		*/
		IEditorPart editor= EditorTestHelper.openInEditor(inputFile, true);
		assertNotNull(editor);
		assertTrue(editor instanceof CEditor);
		CEditor ceditor= (CEditor) editor;
		IEditorInput editorInput = ceditor.getEditorInput();
		StyledText fTextWidget= ceditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		Accessor fAccessor= new Accessor(fTextWidget, StyledText.class);
		IDocument fDocument= ceditor.getDocumentProvider().getDocument(((IEditorPart)ceditor).getEditorInput());
		assertNotNull(fDocument);

		//different to BasicCEditorTest#setCaret
		//***ceditor.setSelection(new mySourceRange(cursorpos),false);
		ceditor.getSelectionProvider().setSelection(new TextSelection(cursorpos, 0));
				
		// execute actions 
		NewTestFunctionAction functionAction=new NewTestFunctionAction();
		MultiTextEdit mEdit = functionAction.createEdit(ceditor, editorInput, fDocument, "newTestFunction");
		
		RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(fDocument, mEdit, TextEdit.CREATE_UNDO);
		processor.performEdits();
		
		//retrieve the edited source
		String results=fDocument.get();
		//handling save dialog prompt 
		ceditor.doSave(new NullProgressMonitor());
		//compare it 
		assertEquals("result unexpected."+testname+"("+cursorpos+")",expectedOutput,results);
		//TODO discarding the changes as clean up
		
		/*
		CCorePlugin.getIndexManager().setIndexerId(cproject,
				IPDOMManager.ID_FAST_INDEXER);
		//TestPlugin.getDefault().getLog().addLogListener(this);
		//Activator.getDefault().getLog().addLogListener(this);
		CCorePlugin.getIndexManager().reindex(cproject);
		boolean joined = CCorePlugin.getIndexManager().joinIndexer(
				IIndexManager.FOREVER, NULL_PROGRESS_MONITOR);
		assertTrue(joined);
		*/
		}catch(Exception e){e.printStackTrace();}
	}
	public static Test suite(){
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.sourceactions");
		ts.addTest(new SourceActionsTest("testNewTestFunctionAll"));
		return ts;
	}
	
}
class ReadTestCase{
	public ArrayList<String> testname=new ArrayList<String>();
	public ArrayList<Integer> cursorpos=new ArrayList<Integer>();
	public ArrayList<String> test=new ArrayList<String>();
	public ArrayList<String> expected=new ArrayList<String>();
	
	enum state{TEST, SAVETEST, EXPECTED, SAVEEXPECTED, CURSOR};
	state m;
		
	public ReadTestCase(){
		StringBuilder builder=new StringBuilder();

		String newline= System.getProperty("line.separator"); 
		try{
		String testnametmp=null;
		BufferedReader br=readTest();
		while(br.ready()){
			String str=br.readLine();
			if(str.startsWith("//test")){
				m=state.SAVEEXPECTED;
				testnametmp=str.substring(6);
			}
			if(str.startsWith("//expected")){
				m=state.SAVETEST;continue;
			}
			if(str.startsWith("//")&& m!=state.SAVEEXPECTED)continue;
			switch(m){
			case TEST:
				builder.append(str+newline);
				break;
			case SAVETEST:
				testname.add(testnametmp);
				test.add(builder.toString());
				builder=new StringBuilder();
				m=state.EXPECTED;
			case EXPECTED:
				builder.append(str+newline);
				break;
			case SAVEEXPECTED:
				if(builder.length()>0){
					expected.add(builder.toString());
					builder=new StringBuilder();
				}
				m=state.TEST;
				break;
			}
		}
		}catch(IOException ioe){ioe.printStackTrace();}
		//handle the last expected
		if(builder.length()>0){
			expected.add(builder.toString());
			builder=new StringBuilder();
		}
		
		parseForCursorPos();
	}
	public static BufferedReader readTest() throws IOException{
		Bundle bundle1 = TestPlugin.getDefault().getBundle();
		Path path1 = new Path("testDefs/sourceActions/newTestfunction.txt");
		URL url1=FileLocator.toFileURL(FileLocator.find(bundle1, path1, null));
		//BufferedInputStream bis=new BufferedInputStream(url1.openStream());
		BufferedReader br=new BufferedReader(new InputStreamReader(url1.openStream()));
		return br;
	}
	public void parseForCursorPos(){
		for(String str:test){
			cursorpos.add(str.indexOf("^"));
		}
		for(int i=0;i<test.size();i++){//remove caret from test
			String str=test.get(i);
			test.remove(i);
			test.add(i, str.replaceAll("[\\^]",""));
		}
		
	}
}//future instead of testing text, parsing AST might bring more flexibility 