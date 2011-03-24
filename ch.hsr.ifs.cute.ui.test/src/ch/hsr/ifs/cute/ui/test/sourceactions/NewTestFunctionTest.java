/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.sourceactions;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Emanuel Graf IFS
 *
 */
@SuppressWarnings("restriction")
public class NewTestFunctionTest extends EditorBaseTest {
	
	private static final String COMMAND_ID = "ch.hsr.ifs.cute.newTestFunctionCommand"; //$NON-NLS-1$

	public NewTestFunctionTest(){
		super("New Test Function"); //$NON-NLS-1$
	}
	
	//
	//void runSuite(){
	//	cute::suite s;
	//
	//}
	
	//void newTest(){
	//	ASSERTM("start writing tests", false);
	//}
	//
	//
	//void runSuite(){
	//	cute::suite s;
	//	s.push_back(CUTE(newTest));
	//
	//}
	public void testNewTestFunction() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "function.cpp"); //$NON-NLS-1$
		CEditor cEditor = runCommand(file, 0, 2, COMMAND_ID);
		type("newTest", 0, 0, cEditor); //$NON-NLS-1$
		cEditor.doSave(new NullProgressMonitor());
		assertFileContent(file, contentsForTest[1].toString());
	}
	
	//
	//void runSuite(){
	//	cute::suite s;
	//
	//}
	
	//void newTest(){
	//	ASSERT(true);
	//}
	//
	//
	//void runSuite(){
	//	cute::suite s;
	//	s.push_back(CUTE(newTest));
	//
	//}
	public void testNewTestFunctionChangeBody() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "body.cpp"); //$NON-NLS-1$
		CEditor cEditor = runCommand(file, 0, 2, COMMAND_ID);
		type("newTest", 0, 0, cEditor); //$NON-NLS-1$
		type('\t', 0, 0, cEditor);
		type("ASSERT(true);", 0, 0, cEditor); //$NON-NLS-1$
		cEditor.doSave(new NullProgressMonitor());
		assertFileContent(file, contentsForTest[1].toString());
	}

}
