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
package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import org.eclipse.core.resources.IFile;

/**
 * @author Emanuel Graf IFS
 *
 */
public class AddTestToSuiteTest extends EditorBaseTest {

	private static final String COMMAND_ID = "ch.hsr.ifs.cute.addTestCommand";

	public AddTestToSuiteTest() {
		super("Add Test to Suite");
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
	public void testAddFunctorToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "functor.cpp");
		runCommand(file, contentsForTest[0].indexOf("functor"), 3, COMMAND_ID );
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
	public void testAddFunctionToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp");
		runCommand(file,contentsForTest[0].indexOf("theTest"), 3, COMMAND_ID);
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
	public void testAddMemberFunctionInSameFileToSuite() throws Exception {
		StringBuffer[] contentsForTest = getContentsForTest(2);
		IFile file = createFile(contentsForTest[0].toString(), "funtion.cpp");
		runCommand(file,contentsForTest[0].indexOf("theTest"), 3, COMMAND_ID);
		assertFileContent(file, contentsForTest[1].toString());
	}
	
	
	//class Suite3{
	//
	//public:
	//	void test();
	//};
	
	//#include "suite.h"
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//
	//	return s;
	//}
	//
	//void Suite3::test()
	//{
	//	ASSERTM("theTest", true);
	//}
	
	//#include "suite.h"
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//
	//cute::suite make_suite_Suite3(){
	//	cute::suite s;
	//	s.push_back(CUTE_SMEMFUN(Suite3, test));
	//
	//	return s;
	//}
	//
	//void Suite3::test()
	//{
	//	ASSERTM("theTest", true);
	//}
	public void testAddMemberFunctionToSuite() throws Exception{
		StringBuffer[] contentsForTest = getContentsForTest(3);
		createFile(contentsForTest[0].toString(), "suite.h");
		IFile file = createFile(contentsForTest[1].toString(), "suite.cpp"); 
		runCommand(file,contentsForTest[1].indexOf("theTest"), 3, COMMAND_ID);
		assertFileContent(file, contentsForTest[2].toString());
	}
	
	

}