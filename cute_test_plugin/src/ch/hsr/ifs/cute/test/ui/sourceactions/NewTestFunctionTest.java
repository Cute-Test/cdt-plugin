/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.test.ui.sourceactions;

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
