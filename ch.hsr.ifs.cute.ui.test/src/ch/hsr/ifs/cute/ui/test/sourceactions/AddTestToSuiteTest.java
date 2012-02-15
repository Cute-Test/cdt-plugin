/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.sourceactions;

import org.eclipse.cdt.internal.core.model.ext.SourceRange;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.ui.UiPlugin;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * 
 */

@SuppressWarnings("restriction")
public class AddTestToSuiteTest extends EditorBaseTest {

	private static final String COMMAND_ID = "ch.hsr.ifs.cute.addTestCommand"; //$NON-NLS-1$

	public AddTestToSuiteTest() {
		super("Add Test to Suite"); //$NON-NLS-1$
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//struct testStruct{
	//	void operator() (){
	//		ASSERTM("functor", true);
	//	}
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//struct testStruct{
	//	void operator() (){
	//		ASSERTM("functor", true);
	//	}
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(testStruct());
	//	return s;
	//}
	//
	public void testAddFunctorToSuite() throws Exception {
		assertEquals(Bundle.ACTIVE, UiPlugin.getDefault().getBundle().getState());
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "functor.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[0].indexOf("functor"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//struct testStruct{
	//	testStruct(int param){}
	//	void operator() (){
	//		ASSERTM("functor", true);
	//	}
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//struct testStruct{
	//	testStruct(int param){}
	//	void operator() (){
	//		ASSERTM("functor", true);
	//	}
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(testStruct(pArAmEtRs_ReQuIrEd));
	//	return s;
	//}
	//
	public void testAddFunctorWithNonDefaultConstructorToSuite() throws Exception {
		assertEquals(Bundle.ACTIVE, UiPlugin.getDefault().getBundle().getState());
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "functor.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[0].indexOf("functor"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE(theTestFunction));
	//	return s;
	//}
	//
	public void testAddFunctionToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[0].indexOf("theTest"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//

	////I add a comment at the beginning
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE(theTestFunction));
	//	return s;
	//}
	//
	public void testAddFunctionToSuiteInDirtyEditorChangeBeforeInsertion() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp"); //$NON-NLS-1$
		final String COMMENT_TO_INSERT = "//I add a comment at the beginning\n";
		type(COMMENT_TO_INSERT, 0, 0, openEditor(file));
		runCommand(file, contentsForTest[0].indexOf("theTest") + COMMENT_TO_INSERT.length(), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//void testOne(){
	//	ASSERT(true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE(testOne));
	//	return s;
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	////I add another comment in between
	//
	//void testOne(){
	//	ASSERT(true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE(testOne));
	//	s.push_back(CUTE(theTestFunction));
	//	return s;
	//}
	//
	public void testAddFunctionToSuiteInDirtyEditorChangeBetweenInsertionAndSelection() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp"); //$NON-NLS-1$

		final CEditor openEditor = openEditor(file);
		openEditor.setSelection(new SourceRange(142, 0), true);

		type("//I add another comment in between\n", 0, 0, openEditor);

		runCommand(contentsForTest[0].indexOf("theTest"), 3, COMMAND_ID, openEditor); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//void theTestFunction(){
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE(theTestFunction));
	//	return s;
	//}
	////I add a comment at the end
	//
	public void testAddFunctionToSuiteInDirtyEditorChangeAfterInsertion() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp"); //$NON-NLS-1$
		final CEditor openEditor = openEditor(file);
		openEditor.setSelection(new SourceRange(205, 0), true);
		type("//I add a comment at the end\n", 0, 0, openEditor);
		runCommand(file, contentsForTest[0].indexOf("theTest"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//struct AllTests{
	//	void testit() { ASSERTM("theTest",true); }
	//};
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//struct AllTests{
	//	void testit() { ASSERTM("theTest",true); }
	//};
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE_SMEMFUN(AllTests, testit));
	//	return s;
	//}
	//
	public void testAddMemberFunctionInSameFileToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[0].indexOf("theTest"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//class Suite3{
	//
	//public:
	//	void test();
	//	};
	//

	//#include "suite.h"
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	return s;
	//}
	//
	//void Suite3::test()
	//{
	//	ASSERTM("theTest", true);
	//}
	//

	//#include "suite.h"
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE_SMEMFUN(Suite3, test));
	//	return s;
	//}
	//
	//void Suite3::test()
	//{
	//	ASSERTM("theTest", true);
	//}
	//
	public void testAddMemberFunctionToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(3);
		createFile(contentsForTest[0].toString(), "suite.h"); //$NON-NLS-1$
		IFile file = createFile(contentsForTest[1].toString(), "suite.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[1].indexOf("theTest"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[2].toString());
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//namespace NS {
	//struct TestClass {
	//	void thisIsATest();
	//};
	//}
	//
	//void NS::TestClass::thisIsATest() {
	//	ASSERTM("start writing tests", false);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//	cute::ide_listener lis;
	//	cute::makeRunner(lis)(s, "The Suite");
	//}
	//

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//namespace NS {
	//struct TestClass {
	//	void thisIsATest();
	//};
	//}
	//
	//void NS::TestClass::thisIsATest() {
	//	ASSERTM("start writing tests", false);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//	s.push_back(CUTE_SMEMFUN(NS::TestClass, thisIsATest));
	//	cute::ide_listener lis;
	//	cute::makeRunner(lis)(s, "The Suite");
	//}
	//
	public void testAddQualifiedMemberFunctionToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "suite.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[0].indexOf("NS::TestClass::thisIsATest()"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

	//#include "suite.h"
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//void testSuccess()
	//{
	//	ASSERTM("theTest", true);
	//}
	//

	//#include "suite.h"
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//void testSuccess()
	//{
	//	ASSERTM("theTest", true);
	//}
	//
	//cute::suite make_suite(){
	//	cute::suite s;
	//	s.push_back(CUTE(testSuccess));
	//	return s;
	//}
	public void testRegisterWithNewSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "suite.cpp"); //$NON-NLS-1$
		runCommand(file, contentsForTest[0].indexOf("testSuccess"), 3, COMMAND_ID); //$NON-NLS-1$
		assertFileContent(file, contentsForTest[1].toString());
	}

}
