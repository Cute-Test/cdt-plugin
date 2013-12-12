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
import org.junit.Test;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * 
 */
@SuppressWarnings("restriction")
public class NewTestFunctionTest extends EditorBaseTest {

	private static final String COMMAND_ID = "ch.hsr.ifs.cute.newTestFunctionCommand";

	//void runSuite(){
	//	cute::suite s;
	//
	//}
	//

	//void newTest(){
	//	ASSERTM("start writing tests", false);
	//}
	//
	//void runSuite(){
	//	cute::suite s;
	//	s.push_back(CUTE(newTest));
	//
	//}
	//
	@Test
	public void testNewTestFunction() throws Exception {
		StringBuilder[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "function.cpp");
		final CEditor cEditor = openEditor(file);
		runCommand(0, 2, COMMAND_ID, cEditor);
		type("newTest", 0, 0, cEditor);
		saveEditor(cEditor);
		assertFileContent(file, contentsForTest[1].toString());
	}

	//void runSuite(){
	//	cute::suite s;
	//
	//}
	//

	//void newTest(){
	//	ASSERT(true);
	//}
	//
	//void runSuite(){
	//	cute::suite s;
	//	s.push_back(CUTE(newTest));
	//
	//}
	//
	@Test
	public void testNewTestFunctionChangeBody() throws Exception {
		StringBuilder[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "body.cpp");
		CEditor cEditor = openEditor(file);
		runCommand(0, 2, COMMAND_ID, cEditor);
		type("newTest", 0, 0, cEditor);
		type('\t', 0, 0, cEditor);
		type("ASSERT(true);", 0, 0, cEditor);
		cEditor.doSave(new NullProgressMonitor());
		assertFileContent(file, contentsForTest[1].toString());
	}

	//

	//void newTest(){
	//	ASSERTM("start writing tests", false);
	//}
	//
	//cute::suite make_suite(){
	//	cute::suite s;
	//	s.push_back(CUTE(newTest));
	//	return s;
	//}
	@Test
	public void testNewTestFunctionMissingSuite() throws Exception {
		StringBuilder[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "function.cpp");
		final CEditor cEditor = openEditor(file);
		cEditor.selectAndReveal(0, 2);
		executeCommand(COMMAND_ID);
		type("newTest", 0, 0, cEditor);
		saveEditor(cEditor);
		assertFileContent(file, contentsForTest[1].toString());
	}

}
