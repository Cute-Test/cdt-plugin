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
import org.eclipse.cdt.ui.tests.text.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cutelauncher.ui.sourceactions.AbstractFunctionAction;
import ch.hsr.ifs.cutelauncher.ui.sourceactions.AddTestFunctiontoSuiteAction;
import ch.hsr.ifs.cutelauncher.ui.sourceactions.AddTestFunctortoSuiteAction;
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
	//FIXME JUnit dblclick not working as tests doesnt have direct src mapping 
	public final static void generateFunctorTest(TestSuite ts){
		final ReadTestCase rtc1=new ReadTestCase("testDefs/sourceActions/addTestfunctor.cpp");
		final AddTestFunctortoSuiteAction functionAction=new AddTestFunctortoSuiteAction();
		for(int i=0;i<rtc1.testname.size();i++){
			//if(5==i)continue;
			final int j=i;
			String displayname=rtc1.testname.get(j).replaceAll("[()]", "*");//JUnit unable to display () as name
			junit.framework.TestCase test = new SourceActionsTest("generateFunctorTest"+i+displayname) {
				@Override
				public void runTest() {
					generateTest(rtc1.testname.get(j),rtc1.test.get(j),rtc1.cursorpos.get(j).intValue(),rtc1.expected.get(j),functionAction);
				}
			};
			ts.addTest(test);
		}
	}
	/*@deprecated
	public final void testAddTestFunctorAll(){
		rtc=new ReadTestCase("testDefs/sourceActions/addTestfunctor.cpp");
		//AddTestFunctiontoSuiteAction functionAction=new AddTestFunctiontoSuiteAction();
		AddTestFunctortoSuiteAction functionAction=new AddTestFunctortoSuiteAction();
		for(int i=0;i<rtc.testname.size();i++){
			generateTest(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i),functionAction);
		}
	}*/
	
	public final void testAddTestFunctionAll(){
		rtc=new ReadTestCase("testDefs/sourceActions/addTestfunction.cpp");
		AddTestFunctiontoSuiteAction functionAction=new AddTestFunctiontoSuiteAction();
		for(int i=0;i<rtc.testname.size();i++){
			generateTest(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i),functionAction);
		}
	}
	
	public final void testNewTestFunctionAll(){
		rtc=new ReadTestCase("testDefs/sourceActions/newTestfunction.txt");
		NewTestFunctionAction functionAction=new NewTestFunctionAction();
		for(int i=0;i<4;i++){
			generateTest(rtc.testname.get(i),rtc.test.get(i),rtc.cursorpos.get(i).intValue(),rtc.expected.get(i),functionAction);
		}//skipped "at end2 with pushback duplicated" at position4 @see NewTestFunctionAction#createEdit 
	}
	
	// org.eclipse.cdt.ui.tests/ui/org.eclipse.cdt.ui.tests.text/BasicCeditor
	//@see org.eclipse.cdt.ui.tests.text.BasicCeditorTest#setUpEditor
	public final void generateTest(String testname,String testSrcCode, int cursorpos, String expectedOutput,AbstractFunctionAction functionAction){
		try{
		IFile inputFile=importFile("A.cpp",testSrcCode);
		/*ITranslationUnit tu= CoreModelUtil.findTranslationUnit(inputFile);
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());	
		IASTTranslationUnit astTu = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		*/
		IEditorPart editor= EditorTestHelper.openInEditor(inputFile, true);
		assertNotNull(editor);
		assertTrue(editor instanceof CEditor);
		ceditor= (CEditor) editor; //dup err
		IEditorInput editorInput = ceditor.getEditorInput();
		/*StyledText fTextWidget= ceditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		Accessor fAccessor= new Accessor(fTextWidget, StyledText.class);*/
		
		Object ele=(editor).getEditorInput();
		IDocumentProvider idp=ceditor.getDocumentProvider();
		IDocument fDocument= idp.getDocument(ele);
		//IDocument fDocument= ceditor.getDocumentProvider().getDocument(((IEditorPart)ceditor).getEditorInput());
		assertNotNull(fDocument);

		//different to BasicCEditorTest#setCaret
		//**faulty**ceditor.setSelection(new mySourceRange(cursorpos),false);
		ceditor.getSelectionProvider().setSelection(new TextSelection(cursorpos, 0));
				
		// execute actions 
		MultiTextEdit mEdit = functionAction.createEdit(ceditor, editorInput, fDocument, "newTestFunction");
		
		assertNotNull(fDocument);
		assertNotNull(mEdit);
		RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(fDocument, mEdit, TextEdit.CREATE_UNDO);
		processor.performEdits();
		
		//retrieve the edited source
		String results=fDocument.get();
		//handling save dialog prompt 
		ceditor.doSave(new NullProgressMonitor());
		//compare it 
		assertEquals("result unexpected."+testname+"("+cursorpos+")",expectedOutput,results);
		//TODO discarding the changes as clean up, instead of writing to disk and then deleting it
		
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
		}catch(Exception e){e.printStackTrace();fail(testname+"\n"+e.getMessage());}
	}
	public static Test suite(){//FIXME unable to continue testing after failing
		TestSuite ts=new TestSuite("ch.hsr.ifs.cutelauncher.ui.sourceactions");
		ts.addTest(new SourceActionsTest("testNewTestFunctionAll"));
		ts.addTest(new SourceActionsTest("testAddTestFunctionAll"));
		//ts.addTest(new SourceActionsTest("testAddTestFunctorAll"));
		generateFunctorTest(ts);
		return ts;
	}
	
}
class ReadTestCase{//TODO checking for null values
	public ArrayList<String> testname=new ArrayList<String>();
	public ArrayList<Integer> cursorpos=new ArrayList<Integer>();
	public ArrayList<String> test=new ArrayList<String>();
	public ArrayList<String> expected=new ArrayList<String>();
	
	enum state{TEST, SAVETEST, EXPECTED, SAVEEXPECTED, CURSOR};
	state m;
		
	public ReadTestCase(String file){
		StringBuilder builder=new StringBuilder();

		String newline= System.getProperty("line.separator"); 
		try{
		String testnametmp=null;
		BufferedReader br=readTest(file);
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
	public static BufferedReader readTest(String file) throws IOException{
		Bundle bundle1 = TestPlugin.getDefault().getBundle();
		Path path1 = new Path(file);
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